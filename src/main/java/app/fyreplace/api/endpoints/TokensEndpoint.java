package app.fyreplace.api.endpoints;

import app.fyreplace.api.cache.DuplicateRequestKeyGenerator;
import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.NewTokenCreation;
import app.fyreplace.api.data.Password;
import app.fyreplace.api.data.RandomCode;
import app.fyreplace.api.data.TokenCreation;
import app.fyreplace.api.data.User;
import app.fyreplace.api.emails.UserConnectionEmail;
import app.fyreplace.api.exceptions.ExplainedFailure;
import app.fyreplace.api.exceptions.ForbiddenException;
import app.fyreplace.api.services.JwtService;
import io.quarkus.cache.CacheResult;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
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
    @RequestBody(required = true)
    @APIResponse(
            responseCode = "201",
            description = "Created",
            content =
                    @Content(
                            mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(implementation = String.class, format = "password")))
    @APIResponse(responseCode = "400", description = "Bad Request")
    @APIResponse(responseCode = "404", description = "Not Found")
    @CacheResult(cacheName = "requests", keyGenerator = DuplicateRequestKeyGenerator.class)
    public Response createToken(@NotNull @Valid final TokenCreation input) {
        final var email = getEmail(input.identifier());
        final var password =
                Password.<Password>find("user.username", input.identifier()).firstResult();
        final RandomCode randomCode;
        final User user;

        try (final var stream = RandomCode.<RandomCode>stream("email", email)) {
            randomCode = stream.filter(rc -> BcryptUtil.matches(input.secret(), rc.code))
                    .findFirst()
                    .orElse(null);
        }

        if (randomCode != null) {
            randomCode.validateEmail();
            user = email.user;
        } else if (password != null && BcryptUtil.matches(input.secret(), password.password)) {
            user = password.user;
        } else {
            throw new BadRequestException();
        }

        user.active = true;
        user.persist();
        return Response.status(Status.CREATED).entity(jwtService.makeJwt(user)).build();
    }

    @GET
    @Path("new")
    @Authenticated
    @APIResponse(
            responseCode = "200",
            description = "OK",
            content =
                    @Content(
                            mediaType = MediaType.TEXT_PLAIN,
                            schema = @Schema(implementation = String.class, format = "password")))
    public String getNewToken() {
        return jwtService.makeJwt(User.getFromSecurityContext(context));
    }

    @POST
    @Path("new")
    @Transactional
    @RequestBody(required = true)
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(responseCode = "400", description = "Bad Request")
    @APIResponse(
            responseCode = "403",
            description = "Not Allowed",
            content =
                    @Content(
                            mediaType = MediaType.APPLICATION_JSON,
                            schema = @Schema(implementation = ExplainedFailure.class)))
    @APIResponse(responseCode = "404", description = "Not Found")
    @CacheResult(cacheName = "requests", keyGenerator = DuplicateRequestKeyGenerator.class)
    public Response createNewToken(
            @NotNull @Valid final NewTokenCreation input,
            @QueryParam("customDeepLinks") final boolean customDeepLinks) {
        final var email = getEmail(input.identifier());
        final var hasPassword = Password.count("user.username", input.identifier()) > 0;

        if (hasPassword) {
            throw new ForbiddenException("user_has_password");
        }

        userConnectionEmail.sendTo(email, customDeepLinks);
        return Response.ok().build();
    }

    private Email getEmail(final String identifier) {
        final var email = Email.<Email>find("email", identifier).firstResult();

        if (email != null) {
            return email;
        }

        final var user = User.findByUsername(identifier);

        if (user == null) {
            throw new NotFoundException();
        } else {
            return user.mainEmail;
        }
    }
}
