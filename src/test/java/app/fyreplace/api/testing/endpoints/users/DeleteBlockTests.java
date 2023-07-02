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
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(UsersEndpoint.class)
public final class DeleteBlockTests extends TransactionalTests {
    @Test
    @TestSecurity(user = "user_0")
    public void deleteBlock() {
        final var user = User.findByUsername("user_0");
        final var otherUser = User.findByUsername("user_1");
        given().delete(otherUser.id + "/isBlocked").then().statusCode(204);
        assertEquals(0, Block.count("source = ?1 and target = ?2", user, otherUser));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void deleteBlockTwice() {
        final var user = User.findByUsername("user_0");
        final var otherUser = User.findByUsername("user_1");
        given().delete(otherUser.id + "/isBlocked").then().statusCode(204);
        given().delete(otherUser.id + "/isBlocked").then().statusCode(204);
        assertEquals(0, Block.count("source = ?1 and target = ?2", user, otherUser));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void deleteBlockWithInvalidUser() {
        final var user = User.findByUsername("user_0");
        final var blockCount = Block.count("source", user);
        given().delete("invalid/isBlocked").then().statusCode(404);
        assertEquals(blockCount, Block.count("source", user));
    }

    @BeforeEach
    @Transactional
    public void beforeEach_createBlock() {
        final var user = User.findByUsername("user_0");
        final var otherUser = User.findByUsername("user_1");
        final var block = new Block();
        block.source = user;
        block.target = otherUser;
        block.persist();
    }
}
