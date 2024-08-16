package app.fyreplace.api.endpoints;

import app.fyreplace.api.cache.DuplicateRequestKeyGenerator;
import app.fyreplace.api.data.User;
import app.fyreplace.api.services.JwtService;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.cache.CacheResult;
import io.quarkus.elytron.security.common.BcryptUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("dev")
@IfBuildProperty(name = "app.local-dev", stringValue = "true")
public final class DevEndpoint {
    @Inject
    JwtService jwtService;

    @GET
    @Path("users/{username}/token")
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
    public String getPasswordHash(@PathParam("password") final String password) {
        return BcryptUtil.bcryptHash(password);
    }
}
