package app.fyreplace.api.endpoints;

import app.fyreplace.api.data.Subscription;
import app.fyreplace.api.data.User;
import io.quarkus.security.Authenticated;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.SecurityContext;
import java.util.UUID;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;

@Path("subscriptions")
public final class SubscriptionsEndpoint {
    @ConfigProperty(name = "app.paging.size")
    int pagingSize;

    @Context
    SecurityContext context;

    @DELETE
    @Path("{id}")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "204", description = "No Content")
    @APIResponse(responseCode = "404", description = "Not Found")
    public void deleteSubscription(@PathParam("id") final UUID id) {
        final var user = User.getFromSecurityContext(context);
        final var subscription = Subscription.<Subscription>findById(id);

        if (subscription == null || !subscription.user.id.equals(user.id)) {
            throw new NotFoundException();
        }

        subscription.markAsRead();
    }

    @GET
    @Path("unread")
    @Authenticated
    @APIResponse(responseCode = "200", description = "OK")
    @APIResponse(responseCode = "400", description = "Bad Request")
    public Iterable<Subscription> listUnreadSubscriptions(@QueryParam("page") @PositiveOrZero final int page) {
        final var user = User.getFromSecurityContext(context);

        try (final var stream =
                Subscription.<Subscription>find("user = ?1 and unreadCommentCount > 0", Subscription.sorting(), user)
                        .page(page, pagingSize)
                        .stream()) {
            return stream.peek(s -> s.post.setCurrentUser(user)).toList();
        }
    }

    @DELETE
    @Path("unread")
    @Authenticated
    @Transactional
    @APIResponse(responseCode = "204", description = "No Content")
    public void clearUnreadSubscriptions() {
        Subscription.markAsRead(User.getFromSecurityContext(context));
    }
}
