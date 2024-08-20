package app.fyreplace.api.services.storage.s3;

import app.fyreplace.api.services.MimeTypeService;
import app.fyreplace.api.services.StorageService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.arc.Unremovable;
import io.quarkus.arc.properties.IfBuildProperty;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

@SuppressWarnings("unused")
@ApplicationScoped
@Unremovable
@IfBuildProperty(name = "app.storage.type", stringValue = "s3")
public final class S3StorageService implements StorageService {
    @Inject
    S3StorageConfig config;

    @Inject
    S3Client client;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    MimeTypeService mimeTypeService;

    public void onStartup(@Observes final StartupEvent event) {
        client.putBucketPolicy(b -> {
            final var statement =
                    new Policy.Statement("Allow", "*", "s3:GetObject", "arn:aws:s3:::" + config.bucket() + "/*");

            try {
                b.bucket(config.bucket())
                        .policy(objectMapper.writeValueAsString(
                                new Policy(Policy.CURRENT_VERSION, List.of(statement))));
            } catch (final JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public byte[] fetch(final String path) throws IOException {
        try {
            return client.getObject(b -> b.bucket(config.bucket()).key(path)).readAllBytes();
        } catch (final NoSuchKeyException e) {
            throw new IOException();
        }
    }

    @Override
    public void store(final String path, final byte[] data) throws IOException {
        final var mime = mimeTypeService.getMimeType(data);
        client.putObject(b -> b.bucket(config.bucket()).key(path).contentType(mime), RequestBody.fromBytes(data));
    }

    @Override
    public void remove(final String path) {
        client.deleteObject(b -> b.bucket(config.bucket()).key(path));
    }

    @Override
    public URI getUri(final String path) throws URISyntaxException {
        return client.utilities()
                .getUrl(b -> {
                    b.bucket(config.bucket()).key(path);
                    config.customEndpoint().ifPresent(b::endpoint);
                })
                .toURI();
    }
}
