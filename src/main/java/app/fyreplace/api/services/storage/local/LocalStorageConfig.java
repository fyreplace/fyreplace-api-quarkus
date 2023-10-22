package app.fyreplace.api.services.storage.local;

import io.smallrye.config.ConfigMapping;
import java.nio.file.Path;

@ConfigMapping(prefix = "app.storage.local")
public interface LocalStorageConfig {
    Path path();
}
