package app.fyreplace.api.testing.endpoints.users;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.equalTo;

import app.fyreplace.api.data.Block;
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
public final class CountBlockedUsersTests extends UserTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void countBlockedUsers() {
        given().get("blocked/count")
                .then()
                .statusCode(200)
                .body(equalTo(String.valueOf(Block.count("source.username = 'user_0'"))));
    }

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        final var user = requireNonNull(User.findByUsername("user_0"));

        for (final var otherUser : User.<User>list("username > 'user_10'")) {
            user.block(otherUser);
        }
    }
}
