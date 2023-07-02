package app.fyreplace.api.testing.endpoints.tokens;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.isA;

import app.fyreplace.api.endpoints.TokensEndpoint;
import app.fyreplace.api.testing.TransactionalTests;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(TokensEndpoint.class)
public final class RetrieveNewTests extends TransactionalTests {
    @Test
    @TestSecurity(user = "user_0")
    public void retrieveNew() {
        given().get("new")
                .then()
                .statusCode(200)
                .contentType(MediaType.TEXT_PLAIN)
                .body(isA(String.class));
    }

    @Test
    public void retrieveNewUnauthenticated() {
        given().get("new").then().statusCode(401);
    }
}
