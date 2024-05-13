package app.fyreplace.api.data;

import app.fyreplace.api.services.MimeTypeService;
import app.fyreplace.api.services.StorageService;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.quarkus.arc.Arc;
import io.sentry.Sentry;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

@Entity
@Table(name = "stored_files")
public class StoredFile extends EntityBase {
    @Transient
    private StorageService storageService;

    @Transient
    private MimeTypeService mimeTypeService;

    @Column(unique = true, nullable = false)
    public String path;

    @Transient
    @Nullable
    private byte[] data;

    @SuppressWarnings("unused")
    public StoredFile() {
        data = null;
        initServices();
    }

    public StoredFile(final String directory, final String name, @Nullable final byte[] data) {
        initServices();
        this.path = Paths.get(directory, name) + mimeTypeService.getExtension(data);
        this.data = data;
    }

    @Override
    public String toString() {
        try {
            return storageService.getUri(path).toString();
        } catch (final URISyntaxException e) {
            Sentry.captureException(e);
            return "";
        }
    }

    public void store(final byte[] data) throws IOException {
        if (data != null) {
            storageService.store(path, data);
        }
    }

    @SuppressWarnings("unused")
    @PostPersist
    final void postPersist() throws IOException {
        if (data != null) {
            store(data);
            data = null;
        }
    }

    @SuppressWarnings("unused")
    @PreRemove
    final void preDestroy() throws IOException {
        storageService.remove(path);
    }

    private void initServices() {
        try (final var storage = Arc.container().instance(StorageService.class);
                final var mimeType = Arc.container().instance(MimeTypeService.class)) {
            storageService = storage.get();
            mimeTypeService = mimeType.get();
        }
    }

    public static final class Serializer extends JsonSerializer<StoredFile> {
        @Override
        public void serialize(
                final StoredFile value, final JsonGenerator generator, final SerializerProvider serializers)
                throws IOException {
            generator.writeString(value.toString());
        }
    }
}
