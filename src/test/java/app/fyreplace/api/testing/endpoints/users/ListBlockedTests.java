package app.fyreplace.api.testing.endpoints.users;

import static io.restassured.RestAssured.given;
import static java.util.stream.IntStream.range;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.in;

import app.fyreplace.api.data.Block;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.UsersEndpoint;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(UsersEndpoint.class)
public final class ListBlockedTests extends UserTestsBase {
    @ConfigProperty(name = "app.paging.size")
    int pagingSize;

    @Test
    @TestSecurity(user = "user_0")
    public void listBlocked() {
        final var user = User.findByUsername("user_0");
        final var response = given().queryParam("page", 0)
                .get("blocked")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(pagingSize));

        try (final var stream = Block.<Block>stream("source", user)) {
            final var blocks = stream.map(block -> block.target.username).toList();
            range(0, pagingSize).forEach(i -> response.body("[" + i + "].username", in(blocks)));
        }
    }

    @Test
    @TestSecurity(user = "user_0")
    public void listBlockedOutOfBounds() {
        given().queryParam("page", -1).get("blocked").then().statusCode(400);
    }

    @Test
    @TestSecurity(user = "user_0")
    public void listBlockedTooFar() {
        given().queryParam("page", 50)
                .get("blocked")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(equalTo("[]"));
    }

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        final var user = User.findByUsername("user_0");

        for (final var otherUser : User.<User>list("username > 'user_10' and active = true")) {
            user.block(otherUser);
        }
    }

    @Override
    public int getActiveUserCount() {
        return 20;
    }
}
