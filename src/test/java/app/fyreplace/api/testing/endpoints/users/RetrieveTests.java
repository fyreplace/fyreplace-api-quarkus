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
import io.restassured.http.ContentType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestHTTPEndpoint(UsersEndpoint.class)
public final class RetrieveTests extends TransactionalTests {
    @ParameterizedTest
    @ValueSource(strings = {"user_0", "user_12", "user_50"})
    public void retrieve(final String username) {
        final var user = User.findByUsername(username);
        given().get(user.id.toString())
                .then()
                .contentType(ContentType.JSON)
                .statusCode(200)
                .body("id", equalTo(user.id.toString()))
                .body("dateCreated", notNullValue())
                .body("username", equalTo(user.username))
                .body("rank", equalTo(User.Rank.CITIZEN.name()))
                .body("avatar", nullValue())
                .body("bio", equalTo(""))
                .body("isBanned", equalTo(false));
    }

    @ParameterizedTest
    @ValueSource(strings = {"nope", "fake", "@", "admin"})
    public void retrieveNonExistent(final String userId) {
        given().get(userId).then().statusCode(404);
    }
}
