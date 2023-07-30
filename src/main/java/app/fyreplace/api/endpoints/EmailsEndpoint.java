package app.fyreplace.api.endpoints;

import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.EmailActivation;
import app.fyreplace.api.data.EmailCreation;
import app.fyreplace.api.data.RandomCode;
import app.fyreplace.api.data.User;
import app.fyreplace.api.emails.EmailVerificationEmail;
import app.fyreplace.api.exceptions.ConflictException;
import app.fyreplace.api.exceptions.ForbiddenException;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
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
import java.util.List;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("emails")
public final class EmailsEndpoint {
    @ConfigProperty(name = "app.paging.size")
    int pagingSize;

    @Inject
    EmailVerificationEmail emailVerificationEmail;

    @GET
    @Authenticated
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "401")
    public List<Email> list(@Context final SecurityContext context, @QueryParam("page") final int page) {
        final var user = User.getFromSecurityContext(context);
        return Email.find("user", Sort.by("email"), user).page(page, pagingSize).list();
    }

    @POST
    @Authenticated
    @Transactional
    @Consumes(MediaType.APPLICATION_JSON)
    @APIResponse(
            responseCode = "201",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Email.class)))
    @APIResponse(responseCode = "400")
    @APIResponse(responseCode = "401")
    @APIResponse(responseCode = "409")
    public Response create(@Context final SecurityContext context, @Valid @NotNull final EmailCreation input) {
        if (Email.count("email", input.email()) > 0) {
            throw new ConflictException("email_taken");
        }

        final var email = new Email();
        email.user = User.getFromSecurityContext(context);
        email.email = input.email();
        email.persist();
        emailVerificationEmail.sendTo(email);
        return Response.status(Status.CREATED).entity(email).build();
    }

    @DELETE
    @Path("{id}")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "204")
    @APIResponse(responseCode = "401")
    @APIResponse(responseCode = "403")
    @APIResponse(responseCode = "404")
    public void delete(@Context final SecurityContext context, @PathParam("id") final UUID id) {
        final var user = User.getFromSecurityContext(context);
        final var email = Email.<Email>find("user = ?1 and id = ?2", user, id).firstResult();

        if (email == null) {
            throw new NotFoundException();
        } else if (email.isMain()) {
            throw new ForbiddenException("email_is_main");
        }

        email.delete();
    }

    @POST
    @Path("{id}/isMain")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "403")
    @APIResponse(responseCode = "404")
    public Response setMain(@Context final SecurityContext context, @PathParam("id") final UUID id) {
        final var user = User.getFromSecurityContext(context);
        final var email = Email.<Email>find("user = ?1 and id = ?2", user, id).firstResult();

        if (email == null) {
            throw new NotFoundException();
        } else if (!email.isVerified) {
            throw new ForbiddenException("email_not_verified");
        }

        user.mainEmail = email;
        user.persist();
        return Response.ok().build();
    }

    @POST
    @Path("activation")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "400")
    @APIResponse(responseCode = "401")
    @APIResponse(responseCode = "404")
    public Response activate(@Context final SecurityContext context, @NotNull @Valid final EmailActivation input) {
        var email = Email.<Email>find("email", input.email()).firstResult();
        final var randomCode = RandomCode.<RandomCode>find("email = ?1 and code = ?2", email, input.code())
                .firstResult();

        if (randomCode == null) {
            throw new NotFoundException();
        }

        randomCode.email.isVerified = true;
        randomCode.email.persist();
        randomCode.delete();
        return Response.ok().build();
    }
}