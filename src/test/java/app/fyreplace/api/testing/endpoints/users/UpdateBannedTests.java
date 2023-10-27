package app.fyreplace.api.testing.endpoints.users;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.UsersEndpoint;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(UsersEndpoint.class)
public final class UpdateBannedTests extends UserTestsBase {
    @Test
    @TestSecurity(user = "user_0", roles = "MODERATOR")
    @Transactional
    public void updateBannedAsModerator() {
        final var user = requireNonNull(User.findByUsername("user_1"));
        given().put(user.id + "/banned").then().statusCode(200);
        user.refresh();
        assertTrue(user.banned);
        assertEquals(User.BanCount.ONCE, user.banCount);
    }

    @Test
    @TestSecurity(user = "user_0")
    @Transactional
    public void updateBannedAsUser() {
        final var user = requireNonNull(User.findByUsername("user_1"));
        given().put(user.id + "/banned").then().statusCode(403);
        user.refresh();
        assertFalse(user.banned);
        assertEquals(User.BanCount.NEVER, user.banCount);
    }

    @Test
    @Transactional
    public void updateBannedUnauthenticated() {
        final var user = requireNonNull(User.findByUsername("user_1"));
        given().put(user.id + "/banned").then().statusCode(401);
        user.refresh();
        assertFalse(user.banned);
        assertEquals(User.BanCount.NEVER, user.banCount);
    }

    @Test
    @TestSecurity(user = "user_0", roles = "MODERATOR")
    @Transactional
    public void updateBannedTwiceAsModerator() {
        final var user = requireNonNull(User.findByUsername("user_2"));
        given().put(user.id + "/banned").then().statusCode(200);
        user.refresh();
        assertTrue(user.banned);
        assertEquals(User.BanCount.ONE_TOO_MANY, user.banCount);
    }

    @Test
    @TestSecurity(user = "user_0")
    @Transactional
    public void updateBannedTwiceAsUser() {
        final var user = requireNonNull(User.findByUsername("user_2"));
        given().put(user.id + "/banned").then().statusCode(403);
        user.refresh();
        assertFalse(user.banned);
        assertEquals(User.BanCount.ONCE, user.banCount);
    }

    @Test
    @Transactional
    public void updateBannedTwiceUnauthenticated() {
        final var user = requireNonNull(User.findByUsername("user_2"));
        given().put(user.id + "/banned").then().statusCode(401);
        user.refresh();
        assertFalse(user.banned);
        assertEquals(User.BanCount.ONCE, user.banCount);
    }

    @Test
    @TestSecurity(user = "user_0", roles = "MODERATOR")
    @Transactional
    public void updateBannedAlreadyBannedAsModerator() {
        final var user = requireNonNull(User.findByUsername("user_3"));
        given().put(user.id + "/banned").then().statusCode(200);
        user.refresh();
        assertTrue(user.banned);
        assertEquals(User.BanCount.ONCE, user.banCount);
    }

    @Test
    @TestSecurity(user = "user_0")
    @Transactional
    public void updateBannedAlreadyBannedAsUser() {
        final var user = requireNonNull(User.findByUsername("user_3"));
        given().put(user.id + "/banned").then().statusCode(403);
        user.refresh();
        assertTrue(user.banned);
        assertEquals(User.BanCount.ONCE, user.banCount);
    }

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        var user = requireNonNull(User.findByUsername("user_2"));
        user.banned = false;
        user.banCount = User.BanCount.ONCE;
        user.persist();

        user = requireNonNull(User.findByUsername("user_3"));
        user.banned = true;
        user.banCount = User.BanCount.ONCE;
        user.persist();
    }

    @Override
    public int getActiveUserCount() {
        return 20;
    }
}
