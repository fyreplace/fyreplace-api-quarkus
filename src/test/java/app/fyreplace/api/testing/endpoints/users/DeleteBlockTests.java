package app.fyreplace.api.testing.endpoints.users;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.fyreplace.api.data.Block;
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
public final class DeleteBlockTests extends TransactionalTests {
    @Test
    @TestSecurity(user = "user_0")
    public void deleteBlock() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        final var otherUser = requireNonNull(User.findByUsername("user_1"));
        assertTrue(user.isBlocking(otherUser));
        given().delete(otherUser.id + "/blocked").then().statusCode(204);
        assertFalse(user.isBlocking(otherUser));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void deleteBlockTwice() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        final var otherUser = requireNonNull(User.findByUsername("user_1"));
        assertTrue(user.isBlocking(otherUser));
        given().delete(otherUser.id + "/blocked").then().statusCode(204);
        given().delete(otherUser.id + "/blocked").then().statusCode(204);
        assertFalse(user.isBlocking(otherUser));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void deleteBlockWithInvalidUser() {
        final var user = User.findByUsername("user_0");
        final var blockCount = Block.count("source", user);
        given().delete("invalid/blocked").then().statusCode(404);
        assertEquals(blockCount, Block.count("source", user));
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
