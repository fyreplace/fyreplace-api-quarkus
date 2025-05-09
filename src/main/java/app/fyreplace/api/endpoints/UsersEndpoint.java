package app.fyreplace.api.endpoints;

import app.fyreplace.api.cache.DuplicateRequestKeyGenerator;
import app.fyreplace.api.data.Block;
import app.fyreplace.api.data.BlockUpdate;
import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.ReportUpdate;
import app.fyreplace.api.data.StoredFile;
import app.fyreplace.api.data.User;
import app.fyreplace.api.data.UserCreation;
import app.fyreplace.api.data.validators.Length;
import app.fyreplace.api.emails.UserActivationEmail;
import app.fyreplace.api.exceptions.ConflictException;
import app.fyreplace.api.exceptions.ExplainedFailure;
import app.fyreplace.api.exceptions.ForbiddenException;
import app.fyreplace.api.exceptions.GoneException;
import app.fyreplace.api.services.ImageService;
import app.fyreplace.api.services.SanitizationService;
import io.quarkus.cache.CacheResult;
import io.quarkus.hibernate.validator.runtime.jaxrs.ViolationReport;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.Authenticated;
import jakarta.annotation.Nullable;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.SecurityContext;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("users")
public final class UsersEndpoint {
    @ConfigProperty(name = "app.paging.size")
    int pagingSize;

    @Inject
    ImageService imageService;

    @Inject
    SanitizationService sanitizationService;

    @Inject
    UserActivationEmail userActivationEmail;

    @Context
    SecurityContext context;

    @POST
    @Transactional
    @RequestBody
    @APIResponse(
            responseCode = "201",
            description = "Created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = User.class)))
    @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ViolationReport.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not Allowed",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ExplainedFailure.class)))
    @APIResponse(
            responseCode = "409",
            description = "Conflict",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ExplainedFailure.class)))
    @CacheResult(cacheName = "requests", keyGenerator = DuplicateRequestKeyGenerator.class)
    public Response createUser(
            @NotNull @Valid final UserCreation input, @QueryParam("customDeepLinks") final boolean customDeepLinks) {
        if (User.FORBIDDEN_USERNAMES.contains(input.username())) {
            throw new ForbiddenException("username_forbidden");
        } else if (User.count("username", input.username()) > 0) {
            throw new ConflictException("username_taken");
        } else if (Email.count("email", input.email()) > 0) {
            throw new ConflictException("email_taken");
        }

        final var user = new User();
        user.username = input.username();
        user.persist();

        final var email = new Email();
        email.user = user;
        email.email = input.email();
        email.persist();
        user.mainEmail = email;
        user.persist();

        userActivationEmail.sendTo(email, customDeepLinks);
        return Response.status(Status.CREATED).entity(user).build();
    }

    @GET
    @Path("{id}")
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(responseCode = "404", description = "Not Found")
    public User getUser(@PathParam("id") final UUID id) {
        final var user = User.<User>findById(id);
        validateUser(user);
        return user;
    }

    @PUT
    @Path("{id}/blocked")
    @Authenticated
    @Transactional
    @RequestBody
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ViolationReport.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not Allowed",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ExplainedFailure.class)))
    @APIResponse(responseCode = "404", description = "Not Found")
    public Response setUserBlocked(@PathParam("id") final UUID id, @NotNull @Valid final BlockUpdate input) {
        final var source = User.getFromSecurityContext(context);
        final var target = User.<User>findById(id);
        validateUser(target);

        if (source.id.equals(target.id)) {
            throw new ForbiddenException("user_is_self");
        } else if (input.blocked()) {
            source.block(target);
        } else {
            source.unblock(target);
        }

        return Response.ok().build();
    }

    @PUT
    @Path("{id}/banned")
    @RolesAllowed("MODERATOR")
    @Transactional
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(responseCode = "404", description = "Not Found")
    public Response setUserBanned(@PathParam("id") final UUID id) {
        final var user = User.<User>findById(id, LockModeType.PESSIMISTIC_WRITE);
        validateUser(user);

        if (!user.banned) {
            if (user.banCount == User.BanCount.NEVER) {
                user.dateBanEnd = Instant.now().plus(Duration.ofDays(7));
                user.banCount = User.BanCount.ONCE;
            } else {
                user.banCount = User.BanCount.ONE_TOO_MANY;
            }

            user.banned = true;
            user.persist();
        }

        return Response.ok().build();
    }

    @PUT
    @Path("{id}/reported")
    @Authenticated
    @Transactional
    @RequestBody
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ViolationReport.class)))
    @APIResponse(
            responseCode = "403",
            description = "Not Allowed",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ExplainedFailure.class)))
    @APIResponse(responseCode = "404", description = "Not Found")
    public Response setUserReported(@PathParam("id") final UUID id, @NotNull @Valid final ReportUpdate input) {
        final var source = User.getFromSecurityContext(context);
        final var target = User.<User>findById(id);
        validateUser(target);

        if (source.id.equals(target.id)) {
            throw new ForbiddenException("user_is_self");
        } else if (input.reported()) {
            target.reportBy(source);
        } else {
            target.absolveBy(source);
        }

        return Response.ok().build();
    }

    @GET
    @Path("current")
    @Authenticated
    @APIResponse(responseCode = "200", description = "OK")
    public User getCurrentUser() {
        return User.getFromSecurityContext(context);
    }

    @DELETE
    @Path("current")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "204", description = "No Content")
    @CacheResult(cacheName = "requests", keyGenerator = DuplicateRequestKeyGenerator.class)
    public Response deleteCurrentUser() {
        User.getFromSecurityContext(context).softDelete();
        return Response.noContent().build();
    }

    @PUT
    @Path("current/bio")
    @Authenticated
    @Transactional
    @RequestBody(content = @Content(mediaType = MediaType.TEXT_PLAIN))
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ViolationReport.class)))
    public String setCurrentUserBio(
            @NotNull @Length(max = User.BIO_MAX_LENGTH) @Schema(maxLength = User.BIO_MAX_LENGTH) @Valid
                    final String input) {
        final var user = User.getFromSecurityContext(context, LockModeType.PESSIMISTIC_READ);
        user.bio = sanitizationService.sanitize(input);
        user.persist();
        return user.bio;
    }

    @PUT
    @Path("current/avatar")
    @Authenticated
    @Transactional
    @RequestBody(content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM))
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(responseCode = "413", description = "Payload Too Large")
    @APIResponse(responseCode = "415", description = "Unsupported Media Type")
    public String setCurrentUserAvatar(final byte[] input) {
        imageService.validate(input);
        final var user = User.getFromSecurityContext(context, LockModeType.PESSIMISTIC_WRITE);
        final var oldAvatar = user.avatar;
        user.avatar = new StoredFile("avatars", imageService.shrink(input));
        user.avatar.persist();
        user.persist();

        if (oldAvatar != null) {
            oldAvatar.delete();
        }

        return user.avatar.toString();
    }

    @DELETE
    @Path("current/avatar")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "204", description = "No Content")
    public void deleteCurrentUserAvatar() {
        final var user = User.getFromSecurityContext(context, LockModeType.PESSIMISTIC_WRITE);

        if (user.avatar != null) {
            user.avatar.delete();
            user.avatar = null;
            user.persist();
        }
    }

    @GET
    @Path("blocked")
    @Authenticated
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ViolationReport.class)))
    public Iterable<User.Profile> listBlockedUsers(@QueryParam("page") @PositiveOrZero final int page) {
        final var user = User.getFromSecurityContext(context);
        final var blocks = Block.<Block>find("source", Sort.by("id"), user);

        try (final var stream = blocks.filter("existing").page(page, pagingSize).stream()) {
            return stream.map(block -> block.target.getProfile()).toList();
        }
    }

    @GET
    @Path("blocked/count")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(responseCode = "200", description = "OK")
    public long countBlockedUsers() {
        return Block.count("source", User.getFromSecurityContext(context));
    }

    private void validateUser(@Nullable final User user) {
        if (user == null || !user.active) {
            throw new NotFoundException();
        } else if (user.deleted) {
            throw new GoneException();
        }

        user.setCurrentUser(User.getFromSecurityContext(context, null, false));
    }
}
