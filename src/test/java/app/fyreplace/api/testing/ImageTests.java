package app.fyreplace.api.testing;

import io.quarkus.test.common.http.TestHTTPResource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public abstract class ImageTests extends TransactionalTests {
    @TestHTTPResource("image.jpeg")
    URL jpeg;

    @TestHTTPResource("image.png")
    URL png;

    @TestHTTPResource("image.webp")
    URL webp;

    @TestHTTPResource("image.gif")
    URL gif;

    @TestHTTPResource("image.txt")
    URL text;

    protected InputStream openStream(final String fileType) throws IOException {
        return (switch (fileType) {
                    case "jpeg" -> jpeg;
                    case "png" -> png;
                    case "webp" -> webp;
                    case "gif" -> gif;
                    case "text" -> text;
                    default -> throw new IllegalArgumentException("Unknown file type: " + fileType);
                })
                .openStream();
    }
}
