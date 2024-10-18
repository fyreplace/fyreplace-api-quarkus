package app.fyreplace.api.services;

import app.fyreplace.api.exceptions.RequestEntityTooLargeException;
import app.fyreplace.api.exceptions.UnsupportedMediaTypeException;
import app.fyreplace.api.services.mimetype.KnownFileType;
import io.quarkus.runtime.configuration.MemorySize;
import jakarta.enterprise.context.ApplicationScoped;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.NoSuchElementException;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
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
        } catch (final NoSuchElementException ignored) {
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

    public byte[] shrink(final byte[] data) throws IOException {
        final var softMaxSize = fileMaxSize.asLongValue();

        if (data.length <= softMaxSize) {
            return data;
        }

        final var scaleFactor = Math.sqrt((double) softMaxSize / data.length);
        final var reader = getFirstValidReader(data);
        final var inputImage = ImageIO.read(new ByteArrayInputStream(data));
        final var width = inputImage.getWidth() * scaleFactor;
        final var height = inputImage.getHeight() * scaleFactor;
        final var scaledImage = inputImage.getScaledInstance((int) width, (int) height, BufferedImage.SCALE_SMOOTH);
        final var outputImage = new BufferedImage((int) width, (int) height, inputImage.getType());
        outputImage.getGraphics().drawImage(scaledImage, 0, 0, null);
        final var outputStream = new ByteArrayOutputStream();
        ImageIO.write(outputImage, reader.getFormatName(), outputStream);
        final var outputData = outputStream.toByteArray();

        if (outputData.length > softMaxSize * 1.5) {
            throw new RequestEntityTooLargeException();
        }

        return outputData;
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
