package app.fyreplace.api.testing.endpoints.users;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.UsersEndpoint;
import app.fyreplace.api.testing.TransactionalTests;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(UsersEndpoint.class)
public final class BannedTests extends TransactionalTests {
    @Test
    @TestSecurity(user = "user_0", roles = "ADMINISTRATOR")
    @Transactional
    public void banWithAdministrator() {
        final var user = User.findByUsername("user_1");
        given().put(user.id + "/banned").then().statusCode(200);
        user.refresh();
        assertTrue(user.banned);
        assertEquals(User.BanCount.ONCE, user.banCount);
    }

    @Test
    @TestSecurity(user = "user_0", roles = "MODERATOR")
    @Transactional
    public void banWithModerator() {
        final var user = User.findByUsername("user_1");
        given().put(user.id + "/banned").then().statusCode(200);
        user.refresh();
        assertTrue(user.banned);
        assertEquals(User.BanCount.ONCE, user.banCount);
    }

    @Test
    @TestSecurity(user = "user_0")
    @Transactional
    public void banWithUser() {
        final var user = User.findByUsername("user_1");
        given().put(user.id + "/banned").then().statusCode(403);
        user.refresh();
        assertFalse(user.banned);
        assertEquals(User.BanCount.NEVER, user.banCount);
    }

    @Test
    @Transactional
    public void banUnauthenticated() {
        final var user = User.findByUsername("user_1");
        given().put(user.id + "/banned").then().statusCode(401);
        user.refresh();
        assertFalse(user.banned);
        assertEquals(User.BanCount.NEVER, user.banCount);
    }

    @Test
    @TestSecurity(user = "user_0", roles = "ADMINISTRATOR")
    @Transactional
    public void banTwiceWithAdministrator() {
        final var user = User.findByUsername("user_2");
        given().put(user.id + "/banned").then().statusCode(200);
        user.refresh();
        assertTrue(user.banned);
        assertEquals(User.BanCount.ONE_TOO_MANY, user.banCount);
    }

    @Test
    @TestSecurity(user = "user_0", roles = "MODERATOR")
    @Transactional
    public void banTwiceWithModerator() {
        final var user = User.findByUsername("user_2");
        given().put(user.id + "/banned").then().statusCode(200);
        user.refresh();
        assertTrue(user.banned);
        assertEquals(User.BanCount.ONE_TOO_MANY, user.banCount);
    }

    @Test
    @TestSecurity(user = "user_0")
    @Transactional
    public void banTwiceWithUser() {
        final var user = User.findByUsername("user_2");
        given().put(user.id + "/banned").then().statusCode(403);
        user.refresh();
        assertFalse(user.banned);
        assertEquals(User.BanCount.ONCE, user.banCount);
    }

    @Test
    @Transactional
    public void banTwiceUnauthenticated() {
        final var user = User.findByUsername("user_2");
        given().put(user.id + "/banned").then().statusCode(401);
        user.refresh();
        assertFalse(user.banned);
        assertEquals(User.BanCount.ONCE, user.banCount);
    }

    @Test
    @TestSecurity(user = "user_0", roles = "ADMINISTRATOR")
    @Transactional
    public void banAlreadyBanned() {
        final var user = User.findByUsername("user_3");
        given().put(user.id + "/banned").then().statusCode(200);
        user.refresh();
        assertTrue(user.banned);
        assertEquals(User.BanCount.ONCE, user.banCount);
    }

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        var user = User.findByUsername("user_2");
        user.banned = false;
        user.banCount = User.BanCount.ONCE;
        user.persist();

        user = User.findByUsername("user_3");
        user.banned = true;
        user.banCount = User.BanCount.ONCE;
        user.persist();
    }
}
