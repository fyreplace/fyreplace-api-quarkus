package app.fyreplace.api.testing.endpoints.users;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.UsersEndpoint;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestHTTPEndpoint(UsersEndpoint.class)
public final class SetCurrentUserBioTests extends UserTestsBase {
    @ParameterizedTest
    @ValueSource(strings = {"Test", "Some random bio", ""})
    @TestSecurity(user = "user_0")
    public void setCurrentUserBio(final String bio) {
        given().contentType(ContentType.TEXT)
                .body(bio)
                .put("current/bio")
                .then()
                .statusCode(200);
        final var user = requireNonNull(User.findByUsername("user_0"));
        assertEquals(bio, user.bio);
    }

    @Test
    @TestSecurity(user = "user_0")
    @Transactional
    public void setCurrentUserBioTooLong() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        final var bio = user.bio;
        given().contentType(ContentType.TEXT)
                .body("a".repeat(3001))
                .put("current/bio")
                .then()
                .statusCode(400);
        user.refresh();
        assertEquals(bio, user.bio);
    }

    @Test
    public void setCurrentUserBioWhileUnauthenticated() {
        given().contentType(ContentType.TEXT)
                .body("Test")
                .put("current/bio")
                .then()
                .statusCode(401);
    }
}
