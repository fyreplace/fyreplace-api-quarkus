package app.fyreplace.api.endpoints;

import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.NewTokenCreation;
import app.fyreplace.api.data.RandomCode;
import app.fyreplace.api.data.TokenCreation;
import app.fyreplace.api.data.User;
import app.fyreplace.api.emails.UserConnectionEmail;
import app.fyreplace.api.services.JwtService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("tokens")
public final class TokensEndpoint {
    @Inject
    JwtService jwtService;

    @Inject
    UserConnectionEmail userConnectionEmail;

    @Context
    SecurityContext context;

    @POST
    @Transactional
    @APIResponse(
            responseCode = "201",
            content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(implementation = String.class)))
    @APIResponse(responseCode = "400")
    @APIResponse(responseCode = "404")
    public Response create(@Valid @NotNull final TokenCreation input) {
        final var email = getEmail(input.identifier());
        final var randomCode = RandomCode.<RandomCode>find("email = ?1 and code = ?2", email, input.code())
                .firstResult();

        if (randomCode == null) {
            throw new NotFoundException();
        }

        randomCode.email.isVerified = true;
        randomCode.email.persist();
        randomCode.delete();
        return Response.status(Status.CREATED).entity(jwtService.makeJwt(email)).build();
    }

    @GET
    @Path("new")
    @Authenticated
    @APIResponse(
            responseCode = "200",
            content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(implementation = String.class)))
    @APIResponse(responseCode = "401")
    public String retrieveNew() {
        return jwtService.makeJwt(User.getFromSecurityContext(context));
    }

    @POST
    @Path("new")
    @Transactional
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "400")
    @APIResponse(responseCode = "404")
    public Response createNew(@NotNull @Valid final NewTokenCreation input) {
        final var email = getEmail(input.identifier());
        userConnectionEmail.sendTo(email);
        return Response.ok().build();
    }

    private Email getEmail(final String identifier) {
        final var email = Email.<Email>find("email", identifier).firstResult();

        if (email != null) {
            return email;
        }

        final var user = User.findByUsername(identifier);

        if (user != null) {
            return user.mainEmail;
        }

        throw new NotFoundException();
    }
}
