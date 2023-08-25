package app.fyreplace.api.endpoints;

import app.fyreplace.api.data.Block;
import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.StoredFile;
import app.fyreplace.api.data.User;
import app.fyreplace.api.data.UserCreation;
import app.fyreplace.api.emails.UserActivationEmail;
import app.fyreplace.api.exceptions.ConflictException;
import app.fyreplace.api.exceptions.ForbiddenException;
import app.fyreplace.api.services.MimeTypeService;
import app.fyreplace.api.services.mimetype.KnownMimeTypes;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.hibernate.validator.constraints.Length;

@Path("users")
public final class UsersEndpoint {
    @ConfigProperty(name = "app.paging.size")
    int pagingSize;

    @Inject
    MimeTypeService mimeTypeService;

    @Inject
    UserActivationEmail userActivationEmail;

    @Context
    SecurityContext context;

    @POST
    @Transactional
    @APIResponse(
            responseCode = "201",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = User.class)))
    @APIResponse(responseCode = "400")
    @APIResponse(responseCode = "403")
    @APIResponse(responseCode = "409")
    public Response create(@Valid @NotNull final UserCreation input) {
        if (User.forbiddenUsernames.contains(input.username())) {
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

        userActivationEmail.sendTo(email);
        return Response.status(Status.CREATED).entity(user).build();
    }

    @GET
    @Path("{id}")
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "404")
    public User retrieve(@PathParam("id") final UUID id) {
        final var user = User.<User>findById(id);

        if (user == null) {
            throw new NotFoundException();
        } else {
            return user;
        }
    }

    @PUT
    @Path("{id}/blocked")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "401")
    @APIResponse(responseCode = "403")
    @APIResponse(responseCode = "404")
    public Response createBlock(@PathParam("id") final UUID id) {
        final var source = User.getFromSecurityContext(context);
        final var target = User.<User>findById(id);

        if (target == null) {
            throw new NotFoundException();
        } else if (source.id.equals(target.id)) {
            throw new ForbiddenException("user_is_self");
        } else if (Block.count("source = ?1 and target = ?2", source, target) == 0) {
            final var block = new Block();
            block.source = source;
            block.target = target;
            block.persist();
        }

        return Response.ok().build();
    }

    @DELETE
    @Path("{id}/blocked")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "204")
    @APIResponse(responseCode = "401")
    @APIResponse(responseCode = "404")
    public void deleteBlock(@PathParam("id") final UUID id) {
        final var source = User.getFromSecurityContext(context);
        final var target = User.<User>findById(id);

        if (target == null) {
            throw new NotFoundException();
        }

        Block.delete("source = ?1 and target = ?2", source, target);
    }

    @PUT
    @Path("{id}/banned")
    @RolesAllowed({"ADMINISTRATOR", "MODERATOR"})
    @Transactional
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "401")
    @APIResponse(responseCode = "403")
    @APIResponse(responseCode = "404")
    public Response updateBanned(@PathParam("id") final UUID id) {
        final var user = User.<User>findById(id, LockModeType.PESSIMISTIC_WRITE);

        if (user == null) {
            throw new NotFoundException();
        } else if (!user.banned) {
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

    @GET
    @Path("me")
    @Authenticated
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "401")
    public User retrieveMe() {
        return retrieve(User.getFromSecurityContext(context).id);
    }

    @PUT
    @Path("me/bio")
    @Authenticated
    @Transactional
    @Consumes(MediaType.TEXT_PLAIN)
    @APIResponse(
            responseCode = "200",
            content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(implementation = String.class)))
    @APIResponse(responseCode = "400")
    @APIResponse(responseCode = "401")
    public String updateMeBio(@NotNull @Length(max = 3000) final String input) {
        final var user = User.getFromSecurityContext(context, LockModeType.PESSIMISTIC_READ);
        user.bio = input;
        user.persist();
        return user.bio;
    }

    @PUT
    @Path("me/avatar")
    @Authenticated
    @Transactional
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @APIResponse(
            responseCode = "200",
            content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(implementation = String.class)))
    @APIResponse(responseCode = "401")
    @APIResponse(responseCode = "413")
    @APIResponse(responseCode = "415")
    public String updateMeAvatar(final File input) throws IOException {
        mimeTypeService.validate(input, KnownMimeTypes.IMAGE);
        final var user = User.getFromSecurityContext(context, LockModeType.PESSIMISTIC_WRITE);
        final var data = Files.readAllBytes(input.toPath());

        if (user.avatar == null) {
            user.avatar = new StoredFile("avatars/" + user.id, data);
        } else {
            user.avatar.store(data);
        }

        user.persist();
        return user.avatar.toString();
    }

    @DELETE
    @Path("me/avatar")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "204")
    @APIResponse(responseCode = "401")
    public void deleteMeAvatar() {
        final var user = User.getFromSecurityContext(context, LockModeType.PESSIMISTIC_WRITE);

        if (user.avatar != null) {
            user.avatar.delete();
            user.avatar = null;
            user.persist();
        }
    }

    @DELETE
    @Path("me")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "204")
    @APIResponse(responseCode = "401")
    public void deleteMe() {
        User.delete("username", context.getUserPrincipal().getName());
    }

    @GET
    @Path("blocked")
    @Authenticated
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "401")
    public List<User.Profile> listBlocked(@QueryParam("page") @PositiveOrZero final int page) {
        return Block.<Block>find("source", Sort.by("id"), User.getFromSecurityContext(context))
                .page(page, pagingSize)
                .stream()
                .map(block -> block.target.getProfile())
                .collect(Collectors.toList());
    }

    @GET
    @Path("blocked/count")
    @Authenticated
    @APIResponse(responseCode = "200")
    public long countBlocked() {
        return Block.count("source", User.getFromSecurityContext(context));
    }
}
