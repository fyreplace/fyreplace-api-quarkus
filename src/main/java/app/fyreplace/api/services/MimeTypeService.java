package app.fyreplace.api.services;

import app.fyreplace.api.exceptions.UnsupportedMediaTypeException;
import app.fyreplace.api.services.mimetype.KnownMimeTypes;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.apache.tika.Tika;

@ApplicationScoped
public final class MimeTypeService {
    public void validate(final byte[] data, final KnownMimeTypes types) throws IOException {
        final var tika = new Tika();

        try (final var stream = new ByteArrayInputStream(data)) {
            final var mimeType = tika.detect(stream);

            if (mimeType == null || !types.types.contains(mimeType)) {
                throw new UnsupportedMediaTypeException("invalid_media_type");
            }
        }
    }
}
