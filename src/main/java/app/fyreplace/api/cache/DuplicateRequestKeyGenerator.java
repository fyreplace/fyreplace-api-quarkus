package app.fyreplace.api.cache;

import static java.util.Objects.requireNonNullElse;

import io.quarkus.cache.CacheKeyGenerator;
import io.quarkus.cache.CompositeCacheKey;
import jakarta.annotation.Nullable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import java.lang.reflect.Method;
import java.util.UUID;

@ApplicationScoped
public class DuplicateRequestKeyGenerator implements CacheKeyGenerator {
    @Context
    HttpHeaders headers;

    @Override
    public @Nullable Object generate(final Method method, final Object... methodParams) {
        final var requestId = requireNonNullElse(headers.getHeaderString("X-Request-Id"), UUID.randomUUID());
        return new CompositeCacheKey(headers.getHeaderString("Authorization"), requestId);
    }
}
