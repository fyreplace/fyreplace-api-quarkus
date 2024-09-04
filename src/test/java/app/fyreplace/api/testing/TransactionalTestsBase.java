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

@WithTestResource(value = DatabaseTestResource.class, restrictToAnnotatedClass = false)
public abstract class TransactionalTestsBase {
    @Inject
    public DataSeeder dataSeeder;

    @Inject
    MockMailbox mailbox;

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
