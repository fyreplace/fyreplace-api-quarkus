package app.fyreplace.api.testing.endpoints.users;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.fyreplace.api.data.Block;
import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.UsersEndpoint;
import app.fyreplace.api.testing.TransactionalTestsBase;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(UsersEndpoint.class)
public final class CreateBlockTests extends TransactionalTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void createBlock() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        final var otherUser = requireNonNull(User.findByUsername("user_1"));
        final var post = Post.<Post>find("author", user).firstResult();
        QuarkusTransaction.requiringNew().run(() -> otherUser.subscribeTo(post));
        assertFalse(user.isBlocking(otherUser));
        given().put(otherUser.id + "/blocked").then().statusCode(200);
        assertTrue(user.isBlocking(otherUser));
        assertFalse(otherUser.isSubscribedTo(post));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void createBlockTwice() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        final var otherUser = requireNonNull(User.findByUsername("user_1"));
        assertFalse(user.isBlocking(otherUser));
        given().put(otherUser.id + "/blocked").then().statusCode(200);
        given().put(otherUser.id + "/blocked").then().statusCode(200);
        assertTrue(user.isBlocking(otherUser));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void createBlockWithInactiveUser() {
        final var user = User.findByUsername("user_0");
        final var otherUser = requireNonNull(User.findByUsername("user_inactive_1"));
        final var blockCount = Block.count("source", user);
        given().put(otherUser.id + "/blocked").then().statusCode(404);
        assertEquals(blockCount, Block.count("source", user));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void createBlockWithInvalidUser() {
        final var user = User.findByUsername("user_0");
        final var blockCount = Block.count("source", user);
        given().put("invalid/blocked").then().statusCode(404);
        assertEquals(blockCount, Block.count("source", user));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void createBlockWithSelf() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        given().put(user.id + "/blocked").then().statusCode(403);
        assertFalse(user.isBlocking(user));
    }
}
