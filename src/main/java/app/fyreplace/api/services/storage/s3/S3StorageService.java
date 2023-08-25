package app.fyreplace.api.services.storage.s3;

import app.fyreplace.api.services.StorageService;
import io.quarkus.arc.Unremovable;
import io.quarkus.arc.properties.IfBuildProperty;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;
import org.jboss.logging.Logger;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;

@SuppressWarnings("unused")
@ApplicationScoped
@Unremovable
@IfBuildProperty(name = "app.storage.type", stringValue = "s3")
public final class S3StorageService implements StorageService {
    private static final Logger logger = Logger.getLogger(S3StorageService.class);

    @Inject
    S3StorageConfig config;

    @Inject
    S3Client client;

    @Override
    public void store(final String path, final byte[] data) {
        client.putObject(b -> b.bucket(config.bucket()).key(path), RequestBody.fromBytes(data));
    }

    @Override
    public void remove(final String path) {
        client.deleteObject(b -> b.bucket(config.bucket()).key(path));
    }

    @Override
    public URI getUri(final String path) {
        try {
            return client.utilities()
                    .getUrl(b -> b.bucket(config.bucket()).key(path))
                    .toURI();
        } catch (final URISyntaxException e) {
            logger.error("Failed to get URI for S3 object", e);
            return null;
        }
    }
}
