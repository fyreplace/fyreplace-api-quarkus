package app.fyreplace.api.endpoints;

import app.fyreplace.api.cache.DuplicateRequestKeyGenerator;
import app.fyreplace.api.data.Comment;
import app.fyreplace.api.data.CommentCreation;
import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.Subscription;
import app.fyreplace.api.data.User;
import app.fyreplace.api.exceptions.ForbiddenException;
import io.quarkus.cache.CacheResult;
import io.quarkus.security.Authenticated;
import jakarta.annotation.Nullable;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.SecurityContext;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("posts/{id}/comments")
public final class CommentsEndpoint {
    @ConfigProperty(name = "app.paging.size")
    int pagingSize;

    @Context
    SecurityContext context;

    @GET
    @Authenticated
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "404")
    public Iterable<Comment> list(@PathParam("id") final UUID id, @QueryParam("page") @PositiveOrZero final int page) {
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, true, false);

        try (final var stream =
                Comment.<Comment>find("post", Comment.sorting(), post).page(page, pagingSize).stream()) {
            return stream.peek(c -> c.setCurrentUser(user)).toList();
        }
    }

    @POST
    @Authenticated
    @Transactional
    @APIResponse(
            responseCode = "201",
            content =
                    @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Comment.class)))
    @APIResponse(responseCode = "404")
    @CacheResult(cacheName = "requests", keyGenerator = DuplicateRequestKeyGenerator.class)
    public Response create(@PathParam("id") final UUID id, @Valid @NotNull final CommentCreation input) {
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, true, false);

        if (input.anonymous() && !user.id.equals(post.author.id)) {
            throw new ForbiddenException("invalid_post_author");
        }

        final var comment = new Comment();
        comment.author = user;
        comment.post = post;
        comment.text = input.text();
        comment.anonymous = input.anonymous();
        comment.persist();
        comment.setCurrentUser(user);
        return Response.status(Status.CREATED).entity(comment).build();
    }

    @DELETE
    @Path("{position}")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "204")
    @APIResponse(responseCode = "404")
    @CacheResult(cacheName = "requests", keyGenerator = DuplicateRequestKeyGenerator.class)
    public Response delete(@PathParam("id") final UUID id, @PathParam("position") @PositiveOrZero final int position) {
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, true, false);
        final var comment = getComment(post, position);

        if (!user.id.equals(comment.author.id)) {
            throw new ForbiddenException("invalid_author");
        }

        comment.softDelete();
        return Response.noContent().build();
    }

    @POST
    @Path("{position}/acknowledge")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "404")
    public Response acknowledge(
            @PathParam("id") final UUID id, @PathParam("position") @PositiveOrZero final int position) {
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, true, false);
        final var comment = getComment(post, position);
        final var subscription = Subscription.<Subscription>find("user = ?1 and post = ?2", user, post)
                .firstResult();

        if (subscription == null) {
            return Response.ok().build();
        }

        if (subscription.lastCommentSeen == null || subscription.lastCommentSeen.compareTo(comment) < 0) {
            subscription.lastCommentSeen = comment;
            subscription.persist();
        }

        return Response.ok().build();
    }

    @GET
    @Path("count")
    @Authenticated
    @APIResponse(responseCode = "200")
    public long count(@PathParam("id") final UUID id, @QueryParam("read") @Nullable final Boolean read) {
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, true, false);

        if (read == null) {
            return Comment.count("post.id", post.id);
        }

        final var subscription = Subscription.<Subscription>find("user = ?1 and post = ?2", user, post)
                .firstResult();
        final var dateComparison = read ? '<' : '>';
        final var idComparison = read ? '=' : '>';
        return subscription != null && subscription.lastCommentSeen != null
                ? Comment.count(
                        "post.id = ?1 and (dateCreated " + dateComparison + " ?2 or (dateCreated = ?2 and id "
                                + idComparison + " ?3))",
                        post.id,
                        subscription.lastCommentSeen.dateCreated,
                        subscription.lastCommentSeen.id)
                : 0;
    }

    private Comment getComment(final Post post, final int position) {
        final var comment = Comment.<Comment>find("post", Comment.sorting(), post)
                .range(position, position + 1)
                .firstResult();

        if (comment == null) {
            throw new NotFoundException();
        }

        return comment;
    }
}
