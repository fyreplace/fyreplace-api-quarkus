package app.fyreplace.api.services.storage.s3;

import io.smallrye.config.ConfigMapping;
import java.net.URI;
import java.util.Optional;

@ConfigMapping(prefix = "app.storage.s3")
public interface S3StorageConfig {
    String bucket();

    Optional<URI> customEndpoint();
}
