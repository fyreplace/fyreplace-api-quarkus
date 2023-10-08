package app.fyreplace.api.testing.endpoints.users;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.UsersEndpoint;
import app.fyreplace.api.testing.TransactionalTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(UsersEndpoint.class)
public final class DeleteMeTests extends TransactionalTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void deleteMe() {
        final var userCount = User.count();
        given().delete("me").then().statusCode(204);
        assertEquals(userCount - 1, User.count());
        assertNull(User.findByUsername("user_0"));
    }

    @Test
    public void deleteMeWithoutAuthentication() {
        final var userCount = User.count();
        given().delete("me").then().statusCode(401);
        assertEquals(userCount, User.count());
    }
}
