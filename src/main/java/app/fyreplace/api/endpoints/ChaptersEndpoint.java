package app.fyreplace.api.endpoints;

import app.fyreplace.api.cache.DuplicateRequestKeyGenerator;
import app.fyreplace.api.data.Chapter;
import app.fyreplace.api.data.ChapterPositionUpdate;
import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.StoredFile;
import app.fyreplace.api.data.User;
import app.fyreplace.api.data.validators.Length;
import app.fyreplace.api.exceptions.ExplainedFailure;
import app.fyreplace.api.exceptions.ForbiddenException;
import app.fyreplace.api.services.ImageService;
import app.fyreplace.api.services.SanitizationService;
import io.quarkus.cache.CacheResult;
import io.quarkus.hibernate.validator.runtime.jaxrs.ViolationReport;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.SecurityContext;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("posts/{id}/chapters")
public final class ChaptersEndpoint {
    @ConfigProperty(name = "app.posts.max-chapter-count")
    int postsMaxChapterCount;

    @Inject
    ImageService imageService;

    @Inject
    SanitizationService sanitizationService;

    @Context
    SecurityContext context;

    @POST
    @Authenticated
    @Transactional
    @APIResponse(
            responseCode = "201",
            description = "Created",
            content =
                    @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Chapter.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not Allowed",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ExplainedFailure.class)))
    @APIResponse(responseCode = "404", description = "Not Found")
    @CacheResult(cacheName = "requests", keyGenerator = DuplicateRequestKeyGenerator.class)
    public Response createChapter(@PathParam("id") final UUID id) {
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, false, true);

        if (Chapter.count("post", post) >= postsMaxChapterCount) {
            throw new ForbiddenException("invalid_chapter_count");
        }

        final var lastExistingChapter =
                Chapter.<Chapter>find("post", Sort.descending("position"), post).firstResult();
        final var chapter = new Chapter();
        chapter.post = post;
        chapter.position = Chapter.positionBetween(lastExistingChapter.position, null);
        chapter.persist();
        return Response.status(Status.CREATED).entity(chapter).build();
    }

    @DELETE
    @Path("{position}")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "204", description = "No Content")
    @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ViolationReport.class)))
    @APIResponse(responseCode = "404", description = "Not Found")
    @CacheResult(cacheName = "requests", keyGenerator = DuplicateRequestKeyGenerator.class)
    public Response deleteChapter(
            @PathParam("id") final UUID id, @PathParam("position") @PositiveOrZero final int position) {
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, false, true);
        getChapter(post, position).delete();
        return Response.noContent().build();
    }

    @PUT
    @Path("{position}/position")
    @Authenticated
    @Transactional
    @RequestBody(required = true)
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ViolationReport.class)))
    @APIResponse(responseCode = "404", description = "Not Found")
    public Response setChapterPosition(
            @PathParam("id") final UUID id,
            @PathParam("position") @PositiveOrZero final int position,
            @NotNull final ChapterPositionUpdate input) {
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, false, true);

        if (position == input.position()) {
            return Response.ok().build();
        }

        try {
            final var chapters = post.getChapters();
            final var chapter = chapters.get(position);
            final var before = input.position() > position ? input.position() : input.position() - 1;
            final var after = input.position() > position ? input.position() + 1 : input.position();
            final var beforePosition = before >= 0 ? chapters.get(before).position : null;
            final var afterPosition = after < chapters.size() ? chapters.get(after).position : null;
            chapter.position = Chapter.positionBetween(beforePosition, afterPosition);
            chapter.persist();
            return Response.ok().build();
        } catch (final IndexOutOfBoundsException e) {
            throw new NotFoundException();
        }
    }

    @PUT
    @Path("{position}/text")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ViolationReport.class)))
    @APIResponse(responseCode = "404", description = "Not Found")
    public String setChapterText(
            @PathParam("id") final UUID id,
            @PathParam("position") @PositiveOrZero final int position,
            @NotNull @Length(max = Chapter.TEXT_MAX_LENGTH) @Schema(maxLength = Chapter.TEXT_MAX_LENGTH) @Valid
                    String input) {
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, false, true);

        try {
            final var chapter = getChapter(post, position);
            chapter.text = sanitizationService.sanitize(input);
            chapter.persist();
            return chapter.text;
        } catch (final IndexOutOfBoundsException e) {
            throw new NotFoundException();
        }
    }

    @PUT
    @Path("{position}/image")
    @Authenticated
    @Transactional
    @RequestBody(required = true, content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM))
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ViolationReport.class)))
    @APIResponse(responseCode = "404", description = "Not Found")
    @APIResponse(responseCode = "413", description = "Payload Too Large")
    @APIResponse(responseCode = "415", description = "Unsupported Media Type")
    public String setChapterImage(
            @PathParam("id") final UUID id,
            @PathParam("position") @PositiveOrZero final int position,
            final byte[] input)
            throws IOException {
        imageService.validate(input);
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, false, true);

        try {
            final var chapter = getChapter(post, position);
            final var image = ImageIO.read(new ByteArrayInputStream(input));
            final var oldImage = chapter.image;
            chapter.width = image.getWidth();
            chapter.height = image.getHeight();
            chapter.image = new StoredFile("chapters", imageService.shrink(input));
            chapter.image.persist();
            chapter.persist();

            if (oldImage != null) {
                oldImage.delete();
            }

            return chapter.image.toString();
        } catch (final IndexOutOfBoundsException e) {
            throw new NotFoundException();
        }
    }

    private Chapter getChapter(final Post post, final int position) {
        try {
            return post.getChapters().get(position);
        } catch (final IndexOutOfBoundsException e) {
            throw new NotFoundException();
        }
    }
}
