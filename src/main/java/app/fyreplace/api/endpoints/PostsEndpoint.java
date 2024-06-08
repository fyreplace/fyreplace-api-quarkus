package app.fyreplace.api.endpoints;

import app.fyreplace.api.cache.DuplicateRequestKeyGenerator;
import app.fyreplace.api.data.Chapter;
import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.PostPublication;
import app.fyreplace.api.data.ReportUpdate;
import app.fyreplace.api.data.Subscription;
import app.fyreplace.api.data.SubscriptionUpdate;
import app.fyreplace.api.data.User;
import app.fyreplace.api.data.Vote;
import app.fyreplace.api.data.VoteCreation;
import app.fyreplace.api.exceptions.ForbiddenException;
import io.quarkus.cache.CacheResult;
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
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(responseCode = "400", description = "Bad Request")
    public Iterable<Post> listPosts(
            @QueryParam("page") @PositiveOrZero final int page,
            @QueryParam("ascending") final boolean ascending,
            @QueryParam("type") @NotNull final PostListingType type) {
        final var user = User.getFromSecurityContext(context);
        final var direction = ascending ? Direction.Ascending : Direction.Descending;
        final var sorting = Post.sorting().direction(direction);
        final var query =
                switch (type) {
                    case SUBSCRIBED_TO -> "from Post p where (select count(*) from Subscription where user = ?1 and post.id = p.id) > 0";
                    case PUBLISHED -> "author = ?1 and published = true";
                    case DRAFTS -> "author = ?1 and published = false";
                };

        try (final var stream =
                Post.<Post>find(query, sorting, user).filter("existing").page(page, pagingSize).stream()) {
            return stream.peek(p -> p.setCurrentUser(user)).toList();
        }
    }

    @POST
    @Authenticated
    @Transactional
    @APIResponse(
            responseCode = "201",
            description = "Created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Post.class)))
    @APIResponse(responseCode = "400", description = "Bad Request")
    @CacheResult(cacheName = "requests", keyGenerator = DuplicateRequestKeyGenerator.class)
    public Response createPost() {
        final var user = User.getFromSecurityContext(context);
        final var post = new Post();
        post.author = user;
        post.persist();
        post.setCurrentUser(user);
        return Response.status(Status.CREATED).entity(post).build();
    }

    @GET
    @Path("{id}")
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(responseCode = "404", description = "Not Found")
    public Post getPost(@PathParam("id") final UUID id) {
        final var user = User.getFromSecurityContext(context, null, false);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, null, null);
        return post;
    }

    @DELETE
    @Path("{id}")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "204", description = "No Content")
    @APIResponse(responseCode = "404", description = "Not Found")
    @CacheResult(cacheName = "requests", keyGenerator = DuplicateRequestKeyGenerator.class)
    public Response deletePost(@PathParam("id") final UUID id) {
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, null, true);

        if (post.published) {
            post.softDelete();
        } else {
            post.delete();
        }

        return Response.noContent().build();
    }

    @PUT
    @Path("{id}/subscribed")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(responseCode = "400", description = "Bad Request")
    @APIResponse(responseCode = "404", description = "Not Found")
    public Response setPostSubscribed(@PathParam("id") final UUID id, @Valid @NotNull final SubscriptionUpdate input) {
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, true, null);

        if (input.subscribed()) {
            user.subscribeTo(post);
        } else {
            user.unsubscribeFrom(post);
        }

        return Response.ok().build();
    }

    @PUT
    @Path("{id}/reported")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(responseCode = "404", description = "Not Found")
    public Response setPostReported(@PathParam("id") final UUID id, @NotNull @Valid final ReportUpdate input) {
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, true, false);

        if (input.reported()) {
            post.reportBy(user);
        } else {
            post.absolveBy(user);
        }

        return Response.ok().build();
    }

    @POST
    @Path("{id}/publish")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(responseCode = "400", description = "Bad Request")
    @APIResponse(responseCode = "404", description = "Not Found")
    @CacheResult(cacheName = "requests", keyGenerator = DuplicateRequestKeyGenerator.class)
    public Response publishPost(@PathParam("id") final UUID id, @Valid @NotNull final PostPublication input) {
        final var user = User.getFromSecurityContext(context);
        final var post = Post.<Post>findById(id);
        Post.validateAccess(post, user, false, true);

        if (Chapter.count("post", post) == 0) {
            throw new ForbiddenException("invalid_chapter_count");
        }

        post.publish(postsStartingLife, input.anonymous());
        return Response.ok().build();
    }

    @POST
    @Path("{id}/vote")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(responseCode = "400", description = "Bad Request")
    @APIResponse(responseCode = "404", description = "Not Found")
    @CacheResult(cacheName = "requests", keyGenerator = DuplicateRequestKeyGenerator.class)
    public Response votePost(@PathParam("id") final UUID id, @Valid @NotNull final VoteCreation input) {
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
        vote.spread = input.spread();
        vote.persist();
        post.life += vote.spread ? 1 : -1;
        post.persist();
        return Response.ok().build();
    }

    @GET
    @Path("count")
    @Authenticated
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(responseCode = "400", description = "Bad Request")
    public long countPosts(@QueryParam("type") @NotNull final PostListingType type) {
        final var user = User.getFromSecurityContext(context);
        return switch (type) {
            case SUBSCRIBED_TO -> Subscription.count("user = ?1 and post.deleted = false", user);
            case PUBLISHED -> Post.count("author = ?1 and published = true and deleted = false", user);
            case DRAFTS -> Post.count("author = ?1 and published = false and deleted = false", user);
        };
    }

    @GET
    @Path("feed")
    @Authenticated
    @APIResponse(responseCode = "200", description = "OK")
    public Iterable<Post> listPostsFeed() {
        final var user = User.getFromSecurityContext(context);

        try (final var stream = Post.<Post>find(
                """
                author != ?1
                and dateCreated > ?2
                and published = true
                and life > 0
                and id not in (select post.id from Vote where user = ?1)
                and author.id not in (select target.id from Block where source = ?1)
                and author.id not in (select source.id from Block where target = ?1)
                """,
                Sort.by("life", "dateCreated", "id"),
                user,
                Instant.now().minus(Post.shelfLife))
                .filter("existing")
                .range(0, 2)
                .stream()) {
            return stream.peek(p -> p.setCurrentUser(user)).toList();
        }
    }

    public enum PostListingType {
        SUBSCRIBED_TO,
        PUBLISHED,
        DRAFTS
    }
}
