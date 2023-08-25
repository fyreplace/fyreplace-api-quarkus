package app.fyreplace.api.endpoints;

import app.fyreplace.api.data.Chapter;
import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.PostPublication;
import app.fyreplace.api.data.Subscription;
import app.fyreplace.api.data.User;
import app.fyreplace.api.data.Vote;
import app.fyreplace.api.data.VoteCreation;
import app.fyreplace.api.exceptions.ForbiddenException;
import io.quarkus.panache.common.Sort;
import io.quarkus.panache.common.Sort.Direction;
import io.quarkus.security.Authenticated;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.SecurityContext;
import java.time.Instant;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("posts")
public final class PostsEndpoint {
    @ConfigProperty(name = "app.paging.size")
    int pagingSize;

    @ConfigProperty(name = "app.posts.starting-life")
    int postsStartingLife;

    @Context
    SecurityContext context;

    @GET
    @Authenticated
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "400")
    public Iterable<Post> list(
            @QueryParam("page") @PositiveOrZero final int page,
            @QueryParam("ascending") final boolean ascending,
            @QueryParam("type") @NotNull final PostListingType type) {
        final var user = User.getFromSecurityContext(context);
        final var direction = ascending ? Direction.Ascending : Direction.Descending;
        final var basicSort = Sort.by("datePublished", "id").direction(direction);

        final var postStream =
                switch (type) {
                    case SUBSCRIBED_TO -> Subscription.<Subscription>find(
                                    "user",
                                    Sort.by("dateLastSeen", "post.datePublished", "post.id")
                                            .direction(direction),
                                    user)
                            .page(page, pagingSize)
                            .stream()
                            .map(s -> s.post);
                    case PUBLISHED -> Post.<Post>find("author = ?1 and datePublished is not null", basicSort, user)
                            .page(page, pagingSize)
                            .stream();
                    case DRAFTS -> Post.<Post>find("author = ?1 and datePublished is null", basicSort, user)
                            .page(page, pagingSize)
                            .stream();
                };

        try (postStream) {
            return postStream.peek(p -> p.setCurrentUser(user)).toList();
        }
    }

    @POST
    @Authenticated
    @Transactional
    @APIResponse(
            responseCode = "201",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Post.class)))
    @APIResponse(responseCode = "400")
    @APIResponse(responseCode = "401")
    public Response create() {
        final var user = User.getFromSecurityContext(context);
        final var post = new Post();
        post.author = user;
        post.persist();
        post.setCurrentUser(user);
        return Response.status(Status.CREATED).entity(post).build();
    }

    @GET
    @Path("{id}")
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "404")
    public Post retrieve(@PathParam("id") final UUID id) {
        final var user = User.getFromSecurityContext(context, null, false);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, null, false);
        return post;
    }

    @DELETE
    @Path("{id}")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "204")
    @APIResponse(responseCode = "401")
    @APIResponse(responseCode = "403")
    @APIResponse(responseCode = "404")
    public void delete(@PathParam("id") final UUID id) {
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, null, true);
        post.delete();
    }

    @POST
    @Path("{id}/publish")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "401")
    @APIResponse(responseCode = "403")
    @APIResponse(responseCode = "404")
    public Response publish(@PathParam("id") final UUID id, @Valid @NotNull final PostPublication input) {
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, false, true);

        if (Chapter.count("post", post) == 0) {
            throw new ForbiddenException("invalid_chapter_count");
        }

        post.publish(postsStartingLife, input.anonymous());
        return Response.ok().build();
    }

    @PUT
    @Path("{id}/isSubscribed")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "401")
    @APIResponse(responseCode = "404")
    public Response createSubscription(@PathParam("id") final UUID id) {
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, true, false);
        user.subscribeTo(post);
        return Response.ok().build();
    }

    @DELETE
    @Path("{id}/isSubscribed")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "204")
    @APIResponse(responseCode = "401")
    @APIResponse(responseCode = "404")
    public void deleteSubscription(@PathParam("id") final UUID id) {
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, true, false);
        user.unsubscribeFrom(post);
    }

    @POST
    @Path("{id}/vote")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "404")
    public Response vote(@PathParam("id") final UUID id, @Valid @NotNull final VoteCreation input) {
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id, LockModeType.PESSIMISTIC_WRITE);
        Post.validateAccess(post, user, true, false);

        if (post.isOld()) {
            throw new ForbiddenException("post_too_old");
        } else if (user.id.equals(post.author.id)) {
            throw new ForbiddenException("invalid_author");
        }

        final var vote = new Vote();
        vote.user = user;
        vote.post = post;
        vote.isSpread = input.isSpread();
        vote.persist();
        post.life += vote.isSpread ? 1 : -1;
        post.persist();
        return Response.ok().build();
    }

    @GET
    @Path("count")
    @Authenticated
    @APIResponse(responseCode = "200")
    public long count(@QueryParam("type") @NotNull final PostListingType type) {
        final var user = User.getFromSecurityContext(context);
        return switch (type) {
            case SUBSCRIBED_TO -> Subscription.count("user", user);
            case PUBLISHED -> Post.count("author = ?1 and datePublished is not null", user);
            case DRAFTS -> Post.count("author = ?1 and datePublished is null", user);
        };
    }

    @GET
    @Path("feed")
    @Authenticated
    @APIResponse(responseCode = "200")
    public Iterable<Post> listFeed() {
        final var user = User.getFromSecurityContext(context);

        try (final var postStream = Post.<Post>find(
                "author != ?1 and datePublished > ?2 and life > 0 and id not in (select post.id from Vote where user = ?1)",
                Sort.by("life", "datePublished", "id"),
                user,
                Instant.now().minus(Post.shelfLife))
                .range(0, 2)
                .stream()) {
            return postStream.peek(p -> p.setCurrentUser(user)).toList();
        }
    }

    public enum PostListingType {
        SUBSCRIBED_TO,
        PUBLISHED,
        DRAFTS
    }
}
