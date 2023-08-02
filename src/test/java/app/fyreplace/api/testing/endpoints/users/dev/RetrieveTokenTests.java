package app.fyreplace.api.testing.endpoints.users.dev;

import static io.restassured.RestAssured.given;

import app.fyreplace.api.endpoints.DevUsersEndpoint;
import app.fyreplace.api.testing.TransactionalTests;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(DevUsersEndpoint.class)
public final class RetrieveTokenTests extends TransactionalTests {
    @Test
    public void retrieveToken() {
        given().get("user_0/token").then().statusCode(401);
    }
}
