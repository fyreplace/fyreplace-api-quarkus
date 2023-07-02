package app.fyreplace.api.testing.endpoints.users;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.Block;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.UsersEndpoint;
import app.fyreplace.api.testing.TransactionalTests;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(UsersEndpoint.class)
public final class CreateBlockTests extends TransactionalTests {
    @Test
    @TestSecurity(user = "user_0")
    public void createBlock() {
        final var user = User.findByUsername("user_0");
        final var otherUser = User.findByUsername("user_2");
        assertEquals(0, Block.count("source = ?1 and target = ?2", user, otherUser));
        given().put(otherUser.id + "/isBlocked").then().statusCode(200);
        assertEquals(1, Block.count("source = ?1 and target = ?2", user, otherUser));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void createBlockTwice() {
        final var user = User.findByUsername("user_0");
        final var otherUser = User.findByUsername("user_1");
        assertEquals(0, Block.count("source = ?1 and target = ?2", user, otherUser));
        given().put(otherUser.id + "/isBlocked").then().statusCode(200);
        given().put(otherUser.id + "/isBlocked").then().statusCode(200);
        assertEquals(1, Block.count("source = ?1 and target = ?2", user, otherUser));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void createBlockWithInvalidUser() {
        final var user = User.findByUsername("user_0");
        final var blockCount = Block.count("source", user);
        given().put("invalid/isBlocked").then().statusCode(404);
        assertEquals(blockCount, Block.count("source", user));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void createBlockWithSelf() {
        final var user = User.findByUsername("user_0");
        given().put(user.id + "/isBlocked").then().statusCode(403);
        assertEquals(0, Block.count("source = ?1 and target = ?2", user, user));
    }
}
