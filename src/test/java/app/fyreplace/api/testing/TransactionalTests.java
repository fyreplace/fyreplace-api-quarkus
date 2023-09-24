package app.fyreplace.api.testing;

import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.dev.DataSeeder;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.MockMailbox;
import io.quarkus.test.common.QuarkusTestResource;
import jakarta.inject.Inject;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

@QuarkusTestResource(DatabaseTestResource.class)
public abstract class TransactionalTests {
    @Inject
    DataSeeder seeder;

    @Inject
    MockMailbox mailbox;

    @BeforeEach
    public void beforeEach() {
        seeder.insertData();
    }

    @AfterEach
    public void afterEach() {
        seeder.deleteData();
        mailbox.clear();
    }

    protected List<Mail> getMailsSentTo(final Email email) {
        return mailbox.getMailsSentTo(email.email);
    }
}
