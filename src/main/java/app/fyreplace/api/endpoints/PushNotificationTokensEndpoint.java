package app.fyreplace.api.endpoints;

import app.fyreplace.api.data.PushNotificationToken;
import app.fyreplace.api.data.PushNotificationTokenCreation;
import app.fyreplace.api.data.User;
import io.quarkus.security.Authenticated;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("push-notification-tokens")
public class PushNotificationTokensEndpoint {
    @Context
    SecurityContext context;

    @PUT
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "200")
    @APIResponse(responseCode = "400")
    public PushNotificationToken update(@Valid final PushNotificationTokenCreation input) {
        final var user = User.getFromSecurityContext(context);
        var token = PushNotificationToken.<PushNotificationToken>find("user = ?1 and token = ?2", user, input.token())
                .firstResult();

        if (token == null) {
            token = new PushNotificationToken();
            token.user = user;
            token.service = input.service();
            token.token = input.token();
            token.persist();
        }

        return token;
    }
}
