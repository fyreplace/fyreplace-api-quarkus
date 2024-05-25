package app.fyreplace.api.services;

import app.fyreplace.api.data.User;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public final class JwtService {
    @ConfigProperty(name = "app.url")
    String appUrl;

    public String makeJwt(final User user) {
        return Jwt.issuer(appUrl)
                .subject(user.id.toString())
                .upn(user.username)
                .groups(user.getGroups())
                .expiresIn(Duration.ofDays(3))
                .sign();
    }
}
