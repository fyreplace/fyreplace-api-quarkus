package app.fyreplace.api.testing.endpoints.emails;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.EmailActivation;
import app.fyreplace.api.data.RandomCode;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.EmailsEndpoint;
import app.fyreplace.api.services.RandomService;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(EmailsEndpoint.class)
public final class ActivateEmailTests extends UserTestsBase {
    @Inject
    RandomService randomService;

    private Email newEmail;

    private RandomCode randomCode;

    @Test
    @TestSecurity(user = "user_0")
    public void activateEmail() {
        given().contentType(ContentType.JSON)
                .body(new EmailActivation(newEmail.email, randomCode.code))
                .post("activate")
                .then()
                .statusCode(200);
        assertEquals(0, RandomCode.count("id", randomCode.id));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void activateEmailWithInvalidEmail() {
        given().contentType(ContentType.JSON)
                .body(new EmailActivation("invalid", randomCode.code))
                .post("activate")
                .then()
                .statusCode(404);
        assertEquals(1, RandomCode.count("id", randomCode.id));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void activateEmailWithOtherEmail() {
        final var otherUser = requireNonNull(User.findByUsername("user_1"));
        given().contentType(ContentType.JSON)
                .body(new EmailActivation(otherUser.mainEmail.email, randomCode.code))
                .post("activate")
                .then()
                .statusCode(404);
        assertEquals(1, RandomCode.count("id", randomCode.id));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void activateEmailWithInvalidCode() {
        given().contentType(ContentType.JSON)
                .body(new EmailActivation(newEmail.email, "invalid"))
                .post("activate")
                .then()
                .statusCode(404);
        assertEquals(1, RandomCode.count("id", randomCode.id));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void activateEmailWithEmptyInput() {
        given().contentType(ContentType.JSON).post("activate").then().statusCode(400);
        assertEquals(1, RandomCode.count("id", randomCode.id));
    }

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        newEmail = new Email();
        newEmail.user = User.findByUsername("user_0");
        newEmail.email = "new_email@example.org";
        newEmail.persist();
        randomCode = new RandomCode();
        randomCode.email = newEmail;
        randomCode.code = randomService.generateCode();
        randomCode.persist();
    }
}
