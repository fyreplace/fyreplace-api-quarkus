package app.fyreplace.api.filters;

import app.fyreplace.api.data.User;
import io.sentry.Sentry;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;

@SuppressWarnings("unused")
@Provider
public final class SentryFilter implements ContainerRequestFilter {
    @Override
    public void filter(final ContainerRequestContext context) {
        final var user = User.getFromSecurityContext(context.getSecurityContext(), null, false);

        Sentry.configureScope(scope -> {
            if (user != null) {
                final var sentryUser = new io.sentry.protocol.User();
                sentryUser.setId(user.id.toString());
                sentryUser.setUsername(user.username);
                sentryUser.setEmail(user.mainEmail != null ? user.mainEmail.email : null);
                scope.setUser(sentryUser);
            } else {
                scope.setUser(null);
            }
        });
    }
}
