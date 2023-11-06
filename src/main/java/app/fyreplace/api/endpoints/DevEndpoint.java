package app.fyreplace.api.endpoints;

import app.fyreplace.api.cache.DuplicateRequestKeyGenerator;
import app.fyreplace.api.data.User;
import app.fyreplace.api.services.JwtService;
import io.quarkus.cache.CacheResult;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("dev")
public final class DevEndpoint {
    @Inject
    JwtService jwtService;

    @GET
    @Path("users/{username}/token")
    @APIResponse(
            responseCode = "200",
            content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(implementation = String.class)))
    @APIResponse(responseCode = "404")
    @CacheResult(cacheName = "requests", keyGenerator = DuplicateRequestKeyGenerator.class)
    public String retrieveToken(@PathParam("username") final String username) {
        final var user = User.findByUsername(username);

        if (user == null) {
            throw new NotFoundException();
        }

        return jwtService.makeJwt(user);
    }

    @GET
    @Path("passwords/{password}/hash")
    @APIResponse(
            responseCode = "200",
            content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(implementation = String.class)))
    @APIResponse(responseCode = "404")
    public String retrievePassword(@PathParam("password") final String password) {
        return BcryptUtil.bcryptHash(password);
    }
}
