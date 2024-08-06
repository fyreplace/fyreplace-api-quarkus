package app.fyreplace.api.services.storage.file;

import app.fyreplace.api.services.storage.LocalStorageServiceBase;
import io.quarkus.arc.Unremovable;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

@SuppressWarnings("unused")
@ApplicationScoped
@Unremovable
@IfBuildProperty(name = "app.storage.type", stringValue = "file")
public final class FileStorageService extends LocalStorageServiceBase {
    @Inject
    FileStorageConfig config;

    @Override
    public byte[] fetch(final String path) throws IOException {
        try (final var reader = new FileInputStream(getFile(path))) {
            return reader.readAllBytes();
        } catch (final FileNotFoundException e) {
            throw new IOException();
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
    public void remove(final String path) throws IOException {
        getFile(path).delete();
    }

    private File getFile(final String path) throws IOException {
        final var file = config.path().resolve(path).toFile();

        if (!file.toPath().normalize().startsWith(config.path().normalize())) {
            throw new IOException();
        }

        return file;
    }
}
