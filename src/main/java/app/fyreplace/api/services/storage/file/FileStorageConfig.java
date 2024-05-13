package app.fyreplace.api.services.storage.file;

import io.smallrye.config.ConfigMapping;
import java.nio.file.Path;

@ConfigMapping(prefix = "app.storage.file")
public interface FileStorageConfig {
    Path path();
}
