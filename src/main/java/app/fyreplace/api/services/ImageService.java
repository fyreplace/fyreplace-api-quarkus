package app.fyreplace.api.services;

import app.fyreplace.api.exceptions.RequestEntityTooLargeException;
import app.fyreplace.api.exceptions.UnsupportedMediaTypeException;
import app.fyreplace.api.services.mimetype.KnownFileType;
import io.quarkus.runtime.configuration.MemorySize;
import jakarta.enterprise.context.ApplicationScoped;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javaxt.io.Image;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public final class ImageService {
    @ConfigProperty(name = "app.storage.limits.max-size")
    MemorySize fileMaxSize;

    public String getMimeType(final byte[] data) throws IOException {
        final var reader = getFirstValidReader(data);
        final var format = reader.getFormatName().toUpperCase();

        try {
            return Arrays.stream(KnownFileType.values())
                    .filter(m -> m.name().equals(format))
                    .findFirst()
                    .orElseThrow()
                    .mime;
        } catch (final NoSuchElementException e) {
            throw new IOException();
        }
    }

    public String getExtension(final byte[] data) {
        try {
            final var mime = getMimeType(data);
            return Arrays.stream(KnownFileType.values())
                    .filter(m -> m.mime.equals(mime))
                    .findFirst()
                    .orElseThrow()
                    .name()
                    .toLowerCase();
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

    public byte[] shrink(final byte[] data) {
        final var softMaxSize = fileMaxSize.asLongValue();

        if (data.length <= softMaxSize) {
            return data;
        }

        final var inputImage = new Image(data);
        final var scaleFactor = Math.sqrt((double) softMaxSize / data.length);
        inputImage.resize((int) (inputImage.getWidth() * scaleFactor), (int) (inputImage.getHeight() * scaleFactor));
        inputImage.rotate();
        final var output = inputImage.getByteArray();

        if (output.length > softMaxSize * 3) {
            throw new RequestEntityTooLargeException();
        }

        return output;
    }

    private ImageReader getFirstValidReader(final byte[] data) throws IOException {
        try (final var input = ImageIO.createImageInputStream(new ByteArrayInputStream(data))) {
            final var readers = ImageIO.getImageReaders(input);

            while (readers.hasNext()) {
                final var reader = readers.next();
                final var format = reader.getFormatName().toUpperCase();

                if (Arrays.stream(KnownFileType.values()).anyMatch(m -> m.name().equals(format))) {
                    return reader;
                }
            }

            throw new IOException();
        }
    }
}
