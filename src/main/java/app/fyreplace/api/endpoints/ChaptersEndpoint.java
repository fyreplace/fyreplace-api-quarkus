package app.fyreplace.api.endpoints;

import static java.util.Objects.requireNonNullElse;

import app.fyreplace.api.data.Chapter;
import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.StoredFile;
import app.fyreplace.api.data.User;
import app.fyreplace.api.exceptions.ForbiddenException;
import app.fyreplace.api.services.MimeTypeService;
import app.fyreplace.api.services.mimetype.KnownMimeTypes;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.metadata.Property;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.hibernate.validator.constraints.Length;

@Path("posts/{id}/chapters")
public final class ChaptersEndpoint {
    @ConfigProperty(name = "app.posts.max-chapter-count")
    int postsMaxChapterCount;

    @Inject
    MimeTypeService mimeTypeService;

    @Context
    SecurityContext context;

    @POST
    @Authenticated
    @Transactional
    @APIResponse(
            responseCode = "201",
            content =
                    @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Chapter.class)))
    @APIResponse(responseCode = "404")
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
    @APIResponse(responseCode = "204")
    @APIResponse(responseCode = "404")
    public void deleteChapter(@PathParam("id") final UUID id, @PathParam("position") final int position) {
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, false, true);
        getChapter(post, position).delete();
    }

    @PUT
    @Path("{position}/position")
    @Authenticated
    @Transactional
    @Consumes(MediaType.TEXT_PLAIN)
    @APIResponse(
            responseCode = "200",
            content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(implementation = Integer.class)))
    @APIResponse(responseCode = "400")
    @APIResponse(responseCode = "404")
    public int updateChapterPosition(
            @PathParam("id") final UUID id, @PathParam("position") final int position, @NotNull final Integer input) {
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, false, true);

        if (position == input) {
            return input;
        }

        try {
            final var chapters = post.getChapters();
            final var chapter = chapters.get(position);
            final var before = input > position ? input : input - 1;
            final var after = input > position ? input + 1 : input;
            final var beforePosition = before >= 0 ? chapters.get(before).position : null;
            final var afterPosition = after < chapters.size() ? chapters.get(after).position : null;
            chapter.position = Chapter.positionBetween(beforePosition, afterPosition);
            chapter.persist();
            return input;
        } catch (final IndexOutOfBoundsException e) {
            throw new NotFoundException();
        }
    }

    @PUT
    @Path("{position}/text")
    @Authenticated
    @Transactional
    @Consumes(MediaType.TEXT_PLAIN)
    @APIResponse(
            responseCode = "200",
            content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(implementation = String.class)))
    @APIResponse(responseCode = "400")
    @APIResponse(responseCode = "404")
    public String updateChapterText(
            @PathParam("id") final UUID id,
            @PathParam("position") final int position,
            @NotNull @Length(max = 500) String input) {
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, false, true);

        try {
            final var chapter = getChapter(post, position);
            chapter.text = input;
            chapter.persist();
            return input;
        } catch (final IndexOutOfBoundsException e) {
            throw new NotFoundException();
        }
    }

    @PUT
    @Path("{position}/image")
    @Authenticated
    @Transactional
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @APIResponse(
            responseCode = "200",
            content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(implementation = String.class)))
    @APIResponse(responseCode = "400")
    @APIResponse(responseCode = "404")
    public String updateChapterImage(
            @PathParam("id") final UUID id, @PathParam("position") final int position, final File input)
            throws IOException {
        mimeTypeService.validate(input, KnownMimeTypes.IMAGE);
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, false, true);
        final var data = Files.readAllBytes(input.toPath());

        try {
            final var chapter = getChapter(post, position);
            final var metadata = mimeTypeService.getMetadata(input);
            final var width = metadata.getInt(Metadata.IMAGE_WIDTH);
            final var height = metadata.getInt(Metadata.IMAGE_LENGTH);

            if (chapter.image == null) {
                chapter.image = new StoredFile("chapters/" + chapter.id, data);
            } else {
                chapter.image.store(data);
            }

            chapter.width = requireNonNullElse(width, metadata.getInt(Property.internalInteger("Image Width")));
            chapter.height = requireNonNullElse(height, metadata.getInt(Property.internalInteger("Image Height")));
            chapter.persist();
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
