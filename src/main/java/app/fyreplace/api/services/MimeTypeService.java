package app.fyreplace.api.services;

import app.fyreplace.api.exceptions.UnsupportedMediaTypeException;
import app.fyreplace.api.services.mimetype.KnownFileType;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import javax.imageio.ImageIO;

@ApplicationScoped
public final class MimeTypeService {
    public String getMimeType(final byte[] data) throws IOException {
        final var input = ImageIO.createImageInputStream(new ByteArrayInputStream(data));
        final var readers = ImageIO.getImageReaders(input);

        while (readers.hasNext()) {
            final var reader = readers.next();
            final var format = reader.getFormatName().toLowerCase();

            try {
                return Arrays.stream(KnownFileType.values())
                        .filter(m -> m.name.equals(format))
                        .findFirst()
                        .orElseThrow()
                        .mime;
            } catch (final NoSuchElementException ignored) {
            }
        }

        throw new IOException();
    }

    public String getExtension(final byte[] data) {
        try {
            final var mime = getMimeType(data);
            return Arrays.stream(KnownFileType.values())
                    .filter(m -> m.mime.equals(mime))
                    .findFirst()
                    .orElseThrow()
                    .name();
        } catch (final IOException | NoSuchElementException e) {
            return "unknown";
        }
    }

    public void validate(final byte[] data) throws UnsupportedMediaTypeException {
        try {
            final var mimeType = getMimeType(data);

            if (Arrays.stream(KnownFileType.values()).noneMatch(m -> m.mime.equals(mimeType))) {
                throw new UnsupportedMediaTypeException();
            }
        } catch (final IOException e) {
            throw new UnsupportedMediaTypeException();
        }
    }
}
