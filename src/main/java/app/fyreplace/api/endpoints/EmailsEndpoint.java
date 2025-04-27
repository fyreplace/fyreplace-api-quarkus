package app.fyreplace.api.endpoints;

import app.fyreplace.api.cache.DuplicateRequestKeyGenerator;
import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.EmailCreation;
import app.fyreplace.api.data.EmailVerification;
import app.fyreplace.api.data.RandomCode;
import app.fyreplace.api.data.User;
import app.fyreplace.api.emails.EmailVerificationEmail;
import app.fyreplace.api.exceptions.ConflictException;
import app.fyreplace.api.exceptions.ExplainedFailure;
import app.fyreplace.api.exceptions.ForbiddenException;
import io.quarkus.cache.CacheResult;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.hibernate.validator.runtime.jaxrs.ViolationReport;
import io.quarkus.panache.common.Sort;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
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
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("emails")
public final class EmailsEndpoint {
    @ConfigProperty(name = "app.paging.size")
    int pagingSize;

    @Inject
    EmailVerificationEmail emailVerificationEmail;

    @Context
    SecurityContext context;

    @GET
    @Authenticated
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ViolationReport.class)))
    public Iterable<Email> listEmails(@QueryParam("page") @PositiveOrZero final int page) {
        final var user = User.getFromSecurityContext(context);
        return Email.find("user", Sort.by("email"), user).page(page, pagingSize).list();
    }

    @POST
    @Authenticated
    @Transactional
    @RequestBody
    @APIResponse(
            responseCode = "201",
            description = "Created",
            content = @Content(mediaType = MediaType.APPLICATION_JSON, schema = @Schema(implementation = Email.class)))
    @APIResponse(
            responseCode = "400",
            description = "Bad Request",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ViolationReport.class)))
    @APIResponse(
            responseCode = "409",
            description = "Conflict",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ExplainedFailure.class)))
    @CacheResult(cacheName = "requests", keyGenerator = DuplicateRequestKeyGenerator.class)
    public Response createEmail(
            @NotNull @Valid final EmailCreation input, @QueryParam("customDeepLinks") final boolean customDeepLinks) {
        if (Email.count("email", input.email()) > 0) {
            throw new ConflictException("email_taken");
        }

        final var email = new Email();
        email.user = User.getFromSecurityContext(context);
        email.email = input.email();
        email.persist();
        emailVerificationEmail.sendTo(email, customDeepLinks);
        return Response.status(Status.CREATED).entity(email).build();
    }

    @DELETE
    @Path("{id}")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "204", description = "No Content")
    @APIResponse(
            responseCode = "403",
            description = "Not Allowed",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ExplainedFailure.class)))
    @APIResponse(responseCode = "404", description = "Not Found")
    @CacheResult(cacheName = "requests", keyGenerator = DuplicateRequestKeyGenerator.class)
    public Response deleteEmail(@PathParam("id") final UUID id) {
        final var user = User.getFromSecurityContext(context);
        final var email = Email.<Email>find("user = ?1 and id = ?2", user, id).firstResult();

        if (email == null) {
            throw new NotFoundException();
        } else if (email.isMain()) {
            throw new ForbiddenException("email_is_main");
        }

        email.delete();
        return Response.noContent().build();
    }

    @PUT
    @Path("{id}/main")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(
            responseCode = "403",
            description = "Not Allowed",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ExplainedFailure.class)))
    @APIResponse(responseCode = "404", description = "Not Found")
    public Response setMainEmail(@PathParam("id") final UUID id) {
        final var user = User.getFromSecurityContext(context);
        final var email = Email.<Email>find("user = ?1 and id = ?2", user, id).firstResult();

        if (email == null) {
            throw new NotFoundException();
        } else if (!email.verified) {
            throw new ForbiddenException("email_not_verified");
        }

        user.mainEmail = email;
        user.persist();
        return Response.ok().build();
    }

    @GET
    @Path("count")
    @Authenticated
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponse(responseCode = "200", description = "OK")
    public long countEmails() {
        return Email.count("user", User.getFromSecurityContext(context));
    }

    @POST
    @Path("verify")
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
    @APIResponse(responseCode = "404", description = "Not Found")
    @CacheResult(cacheName = "requests", keyGenerator = DuplicateRequestKeyGenerator.class)
    public Response verifyEmail(@NotNull @Valid final EmailVerification input) {
        final var email = Email.<Email>find("email", input.email()).firstResult();

        try (final var stream = RandomCode.<RandomCode>stream("email", email)) {
            var randomCode = stream.filter(rc -> BcryptUtil.matches(input.code(), rc.code))
                    .findFirst();

            if (randomCode.isPresent()) {
                randomCode.get().validateEmail();
                return Response.ok().build();
            }
        }

        throw new NotFoundException();
    }
}
