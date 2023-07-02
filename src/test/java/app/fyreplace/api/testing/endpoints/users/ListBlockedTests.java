package app.fyreplace.api.testing.endpoints.users;

import static io.restassured.RestAssured.given;
import static java.util.stream.IntStream.range;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.in;

import app.fyreplace.api.data.Block;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.UsersEndpoint;
import app.fyreplace.api.testing.TransactionalTests;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(UsersEndpoint.class)
public final class ListBlockedTests extends TransactionalTests {
    @ConfigProperty(name = "app.paging.size")
    int pagingSize;

    @Test
    @TestSecurity(user = "user_0")
    public void list() {
        final var user = User.findByUsername("user_0");
        final var response = given().queryParam("page", 0)
                .get("blocked")
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body("size()", equalTo(pagingSize));

        range(0, pagingSize)
                .forEach(i -> response.body(
                        "[" + i + "].username",
                        in(Block.<Block>stream("source", user)
                                .map(block -> block.target.username)
                                .toList())));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void listOutOfBounds() {
        given().queryParam("page", 500)
                .get("blocked")
                .then()
                .statusCode(200)
                .contentType(MediaType.APPLICATION_JSON)
                .body(equalTo("[]"));
    }

    @BeforeEach
    @Transactional
    public void beforeEach_createBlocks() {
        final var user = User.findByUsername("user_0");
        range(10, 50).forEach(i -> {
            final var otherUser = User.findByUsername("user_" + i);
            final var block = new Block();
            block.source = user;
            block.target = otherUser;
            block.persist();
        });
    }
}
