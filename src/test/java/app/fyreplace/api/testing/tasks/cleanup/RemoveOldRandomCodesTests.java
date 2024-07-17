package app.fyreplace.api.testing.tasks.cleanup;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.RandomCode;
import app.fyreplace.api.data.User;
import app.fyreplace.api.services.RandomService;
import app.fyreplace.api.tasks.CleanupTasks;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

@QuarkusTest
public final class RemoveOldRandomCodesTests extends UserTestsBase {
    @Inject
    RandomService randomService;

    @Inject
    CleanupTasks cleanupTasks;

    @Test
    @Transactional
    public void removeOldRandomCodes() {
        final var randomCode = new RandomCode();
        randomCode.email = requireNonNull(User.findByUsername("user_0")).mainEmail;
        randomCode.code = randomService.generateCode();
        randomCode.persist();
        final var randomCodeCount = RandomCode.count();
        RandomCode.update("dateCreated", Instant.now().minus(Duration.ofDays(2)));
        cleanupTasks.removeOldRandomCodes();
        assertEquals(randomCodeCount - 1, RandomCode.count());
    }

    @Test
    public void removeNewRandomCodes() {
        final var randomCodeCount = RandomCode.count();
        cleanupTasks.removeOldRandomCodes();
        assertEquals(randomCodeCount, RandomCode.count());
    }
}
