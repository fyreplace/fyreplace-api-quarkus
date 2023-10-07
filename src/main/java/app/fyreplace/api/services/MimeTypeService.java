package app.fyreplace.api.services;

import app.fyreplace.api.exceptions.UnsupportedMediaTypeException;
import app.fyreplace.api.services.mimetype.KnownMimeTypes;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;

@ApplicationScoped
public final class MimeTypeService {
    public void validate(final byte[] data, final KnownMimeTypes types) {
        final var tika = new Tika();
        final var mimeType = tika.detect(data);

        if (mimeType == null || !types.types.contains(mimeType)) {
            throw new UnsupportedMediaTypeException("invalid_media_type");
        }
    }

    public Metadata getMetadata(final byte[] data) throws IOException {
        final var tika = new Tika();
        final var metadata = new Metadata();

        try (final var stream = new ByteArrayInputStream(data);
                final var ignored = tika.parse(stream, metadata)) {
            return metadata;
        }
    }
}
