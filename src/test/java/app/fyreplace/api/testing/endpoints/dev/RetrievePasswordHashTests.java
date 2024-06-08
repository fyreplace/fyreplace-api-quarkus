package app.fyreplace.api.testing.endpoints.dev;

import static io.restassured.RestAssured.given;

import app.fyreplace.api.endpoints.DevEndpoint;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(DevEndpoint.class)
public final class RetrievePasswordHashTests extends UserTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void getPasswordHash() {
        given().get("passwords/hello/hash").then().statusCode(403);
    }
}
