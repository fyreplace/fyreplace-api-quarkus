package app.fyreplace.api.testing.endpoints.tokens;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.isA;

import app.fyreplace.api.endpoints.TokensEndpoint;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(TokensEndpoint.class)
public final class GetNewTokenTests extends UserTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void getNewToken() {
        given().get("new").then().statusCode(200).contentType(ContentType.TEXT).body(isA(String.class));
    }

    @Test
    public void getNewTokenWhileUnauthenticated() {
        given().get("new").then().statusCode(401);
    }
}
