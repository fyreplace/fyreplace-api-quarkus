package app.fyreplace.api.testing.tasks.cleanup;

import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.User;
import app.fyreplace.api.tasks.CleanupTasks;
import app.fyreplace.api.testing.TransactionalTestsBase;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

@QuarkusTest
public final class RemoveOldInactiveUsersTests extends TransactionalTestsBase {
    @Inject
    CleanupTasks cleanupTasks;

    @Test
    @Transactional
    public void removeOldInactiveUsers() {
        final var totalUserCount = User.count();
        final var inactiveUserCount = User.count("active = false");
        User.update("dateCreated = ?1 where active = false", Instant.now().minus(Duration.ofDays(2)));
        cleanupTasks.removeOldInactiveUsers();
        assertEquals(totalUserCount - inactiveUserCount, User.count());
    }

    @Test
    public void removeNewInactiveUsers() {
        final var totalUserCount = User.count();
        cleanupTasks.removeOldInactiveUsers();
        assertEquals(totalUserCount, User.count());
    }
}
