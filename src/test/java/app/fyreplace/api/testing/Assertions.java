package app.fyreplace.api.testing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import io.quarkus.mailer.Mail;
import java.util.List;

public final class Assertions {
    public static <T> void assertSingleEmail(final Class<T> emailClass, final List<Mail> mails) {
        assertEquals(1, mails.size());
        assertInstanceOf(emailClass, mails.getFirst());
    }
}
