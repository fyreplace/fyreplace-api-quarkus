package app.fyreplace.api.services.storage.local;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "app.storage.local")
public interface LocalStorageConfig {
    String path();
}
