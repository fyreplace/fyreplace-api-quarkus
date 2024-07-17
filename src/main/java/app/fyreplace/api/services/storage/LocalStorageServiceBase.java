package app.fyreplace.api.services.storage;

import app.fyreplace.api.endpoints.StoredFilesEndpoint;
import app.fyreplace.api.services.StorageService;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.UriBuilder;
import java.net.URI;
import org.eclipse.microprofile.config.inject.ConfigProperty;

public abstract class LocalStorageServiceBase implements StorageService {
    @ConfigProperty(name = "app.url")
    String appUrl;

    @Override
    public URI getUri(final String path) {
        final var pathBase = StoredFilesEndpoint.class.getAnnotation(Path.class).value();
        return UriBuilder.fromUri(appUrl).path(pathBase).path(path).build();
    }
}
