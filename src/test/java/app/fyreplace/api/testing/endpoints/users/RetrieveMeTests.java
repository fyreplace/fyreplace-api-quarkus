package app.fyreplace.api.testing.endpoints.users;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.UsersEndpoint;
import app.fyreplace.api.testing.TransactionalTests;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(UsersEndpoint.class)
public final class RetrieveMeTests extends TransactionalTests {
    @Test
    @TestSecurity(user = "user_10")
    public void retrieveMe() {
        final var user = User.findByUsername("user_10");
        given().get("/me")
                .then()
                .contentType(MediaType.APPLICATION_JSON)
                .statusCode(200)
                .body("id", equalTo(user.id.toString()))
                .body("username", equalTo(user.username))
                .body("rank", equalTo(User.Rank.CITIZEN.name()))
                .body("avatar", nullValue())
                .body("bio", equalTo(""))
                .body("isBanned", equalTo(false))
                .body("dateCreated", notNullValue());
    }

    @Test
    public void retrieveMeUnauthenticated() {
        given().get("/me").then().statusCode(401);
    }
}
