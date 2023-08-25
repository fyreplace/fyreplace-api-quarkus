package app.fyreplace.api.services.storage.local;

import app.fyreplace.api.services.StorageService;
import io.quarkus.arc.Unremovable;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;

@SuppressWarnings("unused")
@ApplicationScoped
@Unremovable
@IfBuildProperty(name = "app.storage.type", stringValue = "local")
public final class LocalStorageService implements StorageService {
    @Inject
    LocalStorageConfig config;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void store(final String path, final byte[] data) throws IOException {
        final var file = getFile(path);
        file.getParentFile().mkdirs();

        try (final var writer = new FileOutputStream(file)) {
            writer.write(data);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void remove(final String path) {
        getFile(path).delete();
    }

    @Override
    public URI getUri(final String path) {
        return getFile(path).toURI();
    }

    private File getFile(final String path) {
        return Paths.get(config.path(), path).toFile();
    }
}
