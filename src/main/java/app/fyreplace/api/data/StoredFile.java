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
import jakarta.persistence.PostRemove;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Entity
@Table(name = "stored_files")
public class StoredFile extends EntityBase {
    @Transient
    private StorageService storageService;

    @Transient
    private MimeTypeService mimeTypeService;

    @Column(unique = true, nullable = false)
    @Schema(required = true)
    public String path;

    @Transient
    @Nullable
    private byte[] data;

    @SuppressWarnings("unused")
    public StoredFile() {
        data = null;
        initServices();
    }

    public StoredFile(final String directory, @Nullable final byte[] data) {
        initServices();
        this.path = UriBuilder.fromPath(directory)
                .path(UUID.randomUUID() + "." + mimeTypeService.getExtension(data))
                .build()
                .getPath();
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

    @SuppressWarnings("unused")
    @PrePersist
    final void prePersist() throws IOException {
        if (data != null) {
            storageService.store(path, data);
            data = null;
        }
    }

    @SuppressWarnings("unused")
    @PostRemove
    final void postRemove() throws IOException {
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
                @Nullable final StoredFile value, final JsonGenerator generator, final SerializerProvider serializers)
                throws IOException {
            generator.writeString(value != null ? value.toString() : "");
        }
    }
}
