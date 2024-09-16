package app.fyreplace.api.services.storage;

import app.fyreplace.api.endpoints.StoredFilesEndpoint;
import app.fyreplace.api.services.StorageService;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;

public abstract class LocalStorageServiceBase implements StorageService {
    @Context
    UriInfo uriInfo;

    @Override
    public URI getUri(final String path) {
        final var pathBase = StoredFilesEndpoint.class.getAnnotation(Path.class).value();
        return uriInfo.getBaseUriBuilder().path(pathBase).path(path).build();
    }
}
