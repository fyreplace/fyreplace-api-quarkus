package app.fyreplace.api.services.storage.s3;

import io.smallrye.config.ConfigMapping;
import java.net.URI;

@ConfigMapping(prefix = "app.storage.s3")
public interface S3StorageConfig {
    String bucket();

    URI customDomain();
}
