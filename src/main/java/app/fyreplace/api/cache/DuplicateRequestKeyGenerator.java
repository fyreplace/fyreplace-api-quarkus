package app.fyreplace.api.cache;

import static java.util.Objects.requireNonNullElse;

import io.quarkus.cache.CacheKeyGenerator;
import io.quarkus.cache.CompositeCacheKey;
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
    public Object generate(final Method method, final Object... methodParams) {
        final var authorization = requireNonNullElse(headers.getHeaderString("Authorization"), UUID.randomUUID());
        final var requestId = requireNonNullElse(headers.getHeaderString("X-Request-Id"), UUID.randomUUID());
        return new CompositeCacheKey(method, authorization, requestId);
    }
}
