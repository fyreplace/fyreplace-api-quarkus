package app.fyreplace.api.data;

import app.fyreplace.api.services.StorageService;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import io.quarkus.arc.Arc;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PreRemove;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.io.IOException;

@Entity
@Table(name = "remote_files")
public class StoredFile extends EntityBase {
    @Transient
    private StorageService storageService;

    @Column(unique = true, nullable = false)
    public String path;

    @Transient
    @Nullable
    private byte[] data;

    @SuppressWarnings("unused")
    public StoredFile() {
        data = null;
        initStorageService();
    }

    public StoredFile(final String path, @Nullable final byte[] data) {
        this.path = path;
        this.data = data;
        initStorageService();
    }

    @Override
    public String toString() {
        return storageService.getUri(path).toString();
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

    private void initStorageService() {
        try (final var service = Arc.container().instance(StorageService.class)) {
            storageService = service.get();
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
