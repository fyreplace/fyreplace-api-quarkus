package app.fyreplace.api.testing;

import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.dev.DataSeeder;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.WithTestResource;
import io.quarkus.test.common.http.TestHTTPResource;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

@WithTestResource(value = DatabaseTestResource.class)
public abstract class TransactionalTestsBase {
    @Inject
    public DataSeeder dataSeeder;

    @Inject
    public MockMailbox mailbox;

    @TestHTTPResource("image.jpeg")
    public URL jpegResource;

    @TestHTTPResource("image.png")
    public URL pngResource;

    @TestHTTPResource("image.webp")
    public URL webpResource;

    @TestHTTPResource("image.gif")
    public URL gifResource;

    @TestHTTPResource("image.txt")
    public URL testResource;

    protected static final String FAKE_ID = "00000000-0000-0000-0000-000000000000";

    @BeforeEach
    public void beforeEach() {}

    @AfterEach
    public void afterEach() {
        dataSeeder.deleteData();
        mailbox.clear();
    }

    protected List<Mail> getMailsSentTo(final Email email) {
        return mailbox.getMailsSentTo(email.email);
    }

    protected InputStream openStream(final String fileType) throws IOException {
        return (switch (fileType) {
                    case "jpeg" -> jpegResource;
                    case "png" -> pngResource;
                    case "webp" -> webpResource;
                    case "gif" -> gifResource;
                    case "text" -> testResource;
                    default -> throw new IllegalArgumentException("Unknown file type: " + fileType);
                })
                .openStream();
    }
}
