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
import org.eclipse.microprofile.openapi.annotations.Operation;

@Path("dev")
public final class DevEndpoint {
    @Inject
    JwtService jwtService;

    @GET
    @Path("users/{username}/token")
    @Operation(hidden = true)
    @CacheResult(cacheName = "requests", keyGenerator = DuplicateRequestKeyGenerator.class)
    public String getUserToken(@PathParam("username") final String username) {
        final var user = User.findByUsername(username);

        if (user == null) {
            throw new NotFoundException();
        }

        return jwtService.makeJwt(user);
    }

    @GET
    @Path("passwords/{password}/hash")
    @Operation(hidden = true)
    public String getPasswordHash(@PathParam("password") final String password) {
        return BcryptUtil.bcryptHash(password);
    }
}
