package app.fyreplace.api.services.storage.memory;

import app.fyreplace.api.services.storage.LocalStorageServiceBase;
import io.quarkus.arc.Unremovable;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused")
@ApplicationScoped
@Unremovable
@IfBuildProperty(name = "app.storage.type", stringValue = "memory")
public final class MemoryStorageService extends LocalStorageServiceBase {
    private final Map<String, byte[]> storage = new HashMap<>();

    @Override
    public byte[] fetch(final String path) {
        return storage.get(path);
    }

    @Override
    public void store(final String path, final byte[] data) {
        storage.put(path, data);
    }

    @Override
    public void remove(final String path) {
        storage.remove(path);
    }
}
