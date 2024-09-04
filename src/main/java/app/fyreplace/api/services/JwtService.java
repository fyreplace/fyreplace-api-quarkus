package app.fyreplace.api.services;

import app.fyreplace.api.data.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.net.URI;
import java.time.Duration;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public final class JwtService {
    @ConfigProperty(name = "app.url")
    URI appUrl;

    public static final Duration SHELF_LIFE = Duration.ofDays(7);

    public String makeJwt(final User user) {
        return Jwt.issuer(appUrl.toString())
                .subject(user.id.toString())
                .upn(user.username)
                .groups(user.getGroups())
                .expiresIn(SHELF_LIFE)
                .sign();
    }
}
