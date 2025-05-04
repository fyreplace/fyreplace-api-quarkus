package app.fyreplace.api.testing.tasks.cleanup;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.RandomCode;
import app.fyreplace.api.data.User;
import app.fyreplace.api.services.RandomService;
import app.fyreplace.api.tasks.CleanupTasks;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

@QuarkusTest
public final class RemoveOldUnverifiedEmailsTests extends UserTestsBase {
    @Inject
    RandomService randomService;

    @Inject
    CleanupTasks cleanupTasks;

    @Test
    @Transactional
    public void removeOldUnverifiedEmails() {
        final var email = new Email();
        email.user = requireNonNull(User.findByUsername("user_0"));
        email.email = "unverified@example";
        email.verified = false;
        email.persist();
        final var emailCount = Email.count();
        cleanupTasks.removeOldUnverifiedEmails();
        assertEquals(emailCount - 1, Email.count());
    }

    @Test
    @Transactional
    public void removeNewUnverifiedEmails() {
        final var email = new Email();
        email.user = requireNonNull(User.findByUsername("user_0"));
        email.email = "unverified@example";
        email.verified = false;
        email.persist();
        final var emailCount = Email.count();
        final var randomCode = new RandomCode();
        randomCode.email = email;
        randomCode.code = BcryptUtil.bcryptHash(randomService.generateCode(RandomCode.LENGTH));
        randomCode.persist();
        cleanupTasks.removeOldUnverifiedEmails();
        assertEquals(emailCount, Email.count());
    }
}
