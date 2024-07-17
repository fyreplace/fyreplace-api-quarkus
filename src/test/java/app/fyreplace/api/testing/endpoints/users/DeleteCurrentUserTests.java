package app.fyreplace.api.testing.endpoints.users;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.UsersEndpoint;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(UsersEndpoint.class)
public final class DeleteCurrentUserTests extends UserTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void deleteCurrentUser() {
        given().delete("current").then().statusCode(204);
        final var user = requireNonNull(User.findByUsername("user_0"));
        assertTrue(user.deleted);
    }

    @Test
    public void deleteCurrentUserWhileUnauthenticated() {
        final var userCount = User.count();
        given().delete("current").then().statusCode(401);
        assertEquals(userCount, User.count());
    }
}
