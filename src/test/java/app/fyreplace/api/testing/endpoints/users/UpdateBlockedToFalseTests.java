package app.fyreplace.api.testing.endpoints.users;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.fyreplace.api.data.Block;
import app.fyreplace.api.data.BlockUpdate;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.UsersEndpoint;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(UsersEndpoint.class)
public final class UpdateBlockedToFalseTests extends UserTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void updateBlocked() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        final var otherUser = requireNonNull(User.findByUsername("user_1"));
        assertTrue(user.isBlocking(otherUser));
        given().contentType(ContentType.JSON)
                .body(new BlockUpdate(false))
                .put(otherUser.id + "/blocked")
                .then()
                .statusCode(200);
        assertFalse(user.isBlocking(otherUser));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void updateBlockedTwice() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        final var otherUser = requireNonNull(User.findByUsername("user_1"));
        assertTrue(user.isBlocking(otherUser));
        given().contentType(ContentType.JSON)
                .body(new BlockUpdate(false))
                .put(otherUser.id + "/blocked")
                .then()
                .statusCode(200);
        given().contentType(ContentType.JSON)
                .body(new BlockUpdate(false))
                .put(otherUser.id + "/blocked")
                .then()
                .statusCode(200);
        assertFalse(user.isBlocking(otherUser));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void updateBlockedWithInactiveUser() {
        final var otherUser = requireNonNull(User.findByUsername("user_inactive_1"));
        final var blockCount = Block.count();
        given().contentType(ContentType.JSON)
                .body(new BlockUpdate(false))
                .put(otherUser.id + "/blocked")
                .then()
                .statusCode(404);
        assertEquals(blockCount, Block.count());
    }

    @Test
    @TestSecurity(user = "user_0")
    public void updateBlockedWithInvalidUser() {
        final var blockCount = Block.count();
        given().contentType(ContentType.JSON)
                .body(new BlockUpdate(false))
                .put("invalid/blocked")
                .then()
                .statusCode(404);
        assertEquals(blockCount, Block.count());
    }

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        final var user = requireNonNull(User.findByUsername("user_0"));
        final var otherUser = requireNonNull(User.findByUsername("user_1"));
        user.block(otherUser);
    }
}
