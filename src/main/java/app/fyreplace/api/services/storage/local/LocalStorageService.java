package app.fyreplace.api.services.storage.local;

import app.fyreplace.api.services.StorageService;
import io.quarkus.arc.Unremovable;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Paths;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@SuppressWarnings("unused")
@ApplicationScoped
@Unremovable
@IfBuildProperty(name = "app.storage.type", stringValue = "local")
public final class LocalStorageService implements StorageService {
    @ConfigProperty(name = "app.url")
    String appUrl;

    @Inject
    LocalStorageConfig config;

    @Override
    public byte[] fetch(final String path) throws IOException {
        try (final var reader = new FileInputStream(getFile(path))) {
            return reader.readAllBytes();
        } catch (final FileNotFoundException e) {
            throw new NotFoundException();
        }
    }

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
        return URI.create(appUrl).resolve(Paths.get("stored-files", path).toString());
    }

    private File getFile(final String path) {
        return Paths.get(config.path(), path).toFile();
    }
}
