package app.fyreplace.api.testing.tasks.moderation;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.fyreplace.api.data.User;
import app.fyreplace.api.tasks.ModerationTasks;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

@QuarkusTest
public final class WelcomeBackBannedUsersTests extends UserTestsBase {
    @Inject
    ModerationTasks moderationTasks;

    @Test
    public void welcomeBackUserBannedNever() {
        moderationTasks.welcomeBackBannedUsers();
        final var user = requireNonNull(User.findByUsername("user_0"));
        assertFalse(user.banned);
        assertEquals(User.BanCount.NEVER, user.banCount);
        assertNull(user.dateBanEnd);
    }

    @Test
    public void welcomeBackUserBannedOnceLongAgo() {
        QuarkusTransaction.requiringNew().run(() -> {
            final var user = requireNonNull(User.findByUsername("user_0"));
            user.banned = true;
            user.banCount = User.BanCount.ONCE;
            user.dateBanEnd = Instant.now().minus(Duration.ofDays(1));
            user.persist();
        });
        moderationTasks.welcomeBackBannedUsers();
        final var user = requireNonNull(User.findByUsername("user_0"));
        assertFalse(user.banned);
        assertEquals(User.BanCount.ONCE, user.banCount);
        assertNull(user.dateBanEnd);
    }

    @Test
    public void welcomeBackUserBannedOnceRecently() {
        QuarkusTransaction.requiringNew().run(() -> {
            final var user = requireNonNull(User.findByUsername("user_0"));
            user.banned = true;
            user.banCount = User.BanCount.ONCE;
            user.dateBanEnd = Instant.now().plus(Duration.ofDays(1));
            user.persist();
        });
        moderationTasks.welcomeBackBannedUsers();
        final var user = requireNonNull(User.findByUsername("user_0"));
        assertTrue(user.banned);
        assertEquals(User.BanCount.ONCE, user.banCount);
        assertNotNull(user.dateBanEnd);
    }

    @Test
    public void welcomeBackUserBannedForever() {
        QuarkusTransaction.requiringNew().run(() -> {
            final var user = requireNonNull(User.findByUsername("user_0"));
            user.banned = true;
            user.banCount = User.BanCount.ONE_TOO_MANY;
            user.persist();
        });
        moderationTasks.welcomeBackBannedUsers();
        final var user = requireNonNull(User.findByUsername("user_0"));
        assertTrue(user.banned);
        assertEquals(User.BanCount.ONE_TOO_MANY, user.banCount);
        assertNull(user.dateBanEnd);
    }
}
