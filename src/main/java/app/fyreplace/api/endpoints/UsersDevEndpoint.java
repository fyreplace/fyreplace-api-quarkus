package app.fyreplace.api.endpoints;

import app.fyreplace.api.data.User;
import app.fyreplace.api.services.JwtService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("dev/users")
public final class UsersDevEndpoint {
    @Inject
    JwtService jwtService;

    @GET
    @Path("{username}/token")
    @APIResponse(
            responseCode = "200",
            content = @Content(mediaType = MediaType.TEXT_PLAIN, schema = @Schema(implementation = String.class)))
    public String retrieveToken(@PathParam("username") final String username) {
        return jwtService.makeJwt(User.findByUsername(username));
    }
}
