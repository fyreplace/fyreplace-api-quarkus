package app.fyreplace.api.testing.endpoints.users;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.UsersEndpoint;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(UsersEndpoint.class)
public final class GetCurrentUserTests extends UserTestsBase {
    @Test
    @TestSecurity(user = "user_2")
    public void getCurrentUser() {
        final var user = requireNonNull(User.findByUsername("user_2"));
        given().get("/current")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(user.id.toString()))
                .body("dateCreated", notNullValue())
                .body("username", equalTo(user.username))
                .body("rank", equalTo(User.Rank.CITIZEN.name()))
                .body("avatar", nullValue())
                .body("bio", equalTo(""))
                .body("banned", equalTo(false))
                .body("blocked", equalTo(false));
    }

    @Test
    public void getCurrentUserWhileUnauthenticated() {
        given().get("/current").then().statusCode(401);
    }
}
