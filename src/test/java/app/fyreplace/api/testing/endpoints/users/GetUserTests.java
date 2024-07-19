package app.fyreplace.api.testing.endpoints.users;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.UsersEndpoint;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestHTTPEndpoint(UsersEndpoint.class)
public final class GetUserTests extends UserTestsBase {
    @ParameterizedTest
    @ValueSource(strings = {"user_0", "user_1", "user_2"})
    public void getUserWhileUnauthenticated(final String username) {
        final var user = requireNonNull(User.findByUsername(username));
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
                .body("banned", equalTo(false))
                .body("blocked", equalTo(false));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void getUser() {
        final var user = requireNonNull(User.findByUsername("user_1"));
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
                .body("banned", equalTo(false))
                .body("blocked", equalTo(false));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void getBlockedUser() {
        final var user = requireNonNull(User.findByUsername("user_1"));
        QuarkusTransaction.requiringNew()
                .run(() -> requireNonNull(User.findByUsername("user_0")).block(user));
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
                .body("banned", equalTo(false))
                .body("blocked", equalTo(true));
    }

    @ParameterizedTest
    @ValueSource(strings = {"user_inactive_0", "user_inactive_1", "user_inactive_2"})
    public void getInactiveUser(final String username) {
        final var user = requireNonNull(User.findByUsername(username));
        given().get(user.id.toString()).then().statusCode(404);
    }

    @Test
    public void getDeletedUser() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        QuarkusTransaction.requiringNew().run(() -> User.<User>findById(user.id).softDelete());
        given().get(user.id.toString()).then().statusCode(410);
    }

    @ParameterizedTest
    @ValueSource(strings = {"nope", "fake", "@", "admin", "00000000-0000-0000-0000-000000000000"})
    public void getNonExistentUser(final String userId) {
        given().get(userId).then().statusCode(404);
    }
}
