package app.fyreplace.api.services;

import java.io.IOException;
import java.net.URI;

public interface StorageService {
    void store(final String path, final byte[] data) throws IOException;

    void remove(final String path) throws IOException;

    URI getUri(final String path);
}
