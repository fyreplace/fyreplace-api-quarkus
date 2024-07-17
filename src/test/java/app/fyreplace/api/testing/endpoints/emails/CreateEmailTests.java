package app.fyreplace.api.testing.endpoints.emails;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.EmailCreation;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.EmailsEndpoint;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(EmailsEndpoint.class)
public final class CreateEmailTests extends UserTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void createEmail() {
        final var email = "some_new_email@example.org";
        final var emailCount = Email.count();
        given().contentType(ContentType.JSON)
                .body(new EmailCreation(email))
                .post()
                .then()
                .contentType(ContentType.JSON)
                .statusCode(201)
                .body("email", equalTo(email))
                .body("verified", equalTo(false))
                .body("main", equalTo(false));
        assertEquals(emailCount + 1, Email.count());
    }

    @Test
    @TestSecurity(user = "user_0")
    public void createEmailWithInvalidEmail() {
        final var emailCount = Email.count();
        given().contentType(ContentType.JSON)
                .body(new EmailCreation("invalid"))
                .post()
                .then()
                .contentType(ContentType.JSON)
                .statusCode(400);
        assertEquals(emailCount, Email.count());
    }

    @Test
    @TestSecurity(user = "user_0")
    public void createEmailWithEmptyEmail() {
        final var emailCount = Email.count();
        given().contentType(ContentType.JSON)
                .body(new EmailCreation(""))
                .post()
                .then()
                .contentType(ContentType.JSON)
                .statusCode(400);
        assertEquals(emailCount, Email.count());
    }

    @Test
    @TestSecurity(user = "user_0")
    public void createEmailWithExistingEmail() {
        final var existingUser = requireNonNull(User.findByUsername("user_1"));
        final var emailCount = Email.count();
        given().contentType(ContentType.JSON)
                .body(new EmailCreation(existingUser.mainEmail.email))
                .post()
                .then()
                .contentType(ContentType.JSON)
                .statusCode(409);
        assertEquals(emailCount, Email.count());
    }

    @Test
    @TestSecurity(user = "user_0")
    public void createEmailWithEmptyInput() {
        final var emailCount = Email.count();
        given().contentType(ContentType.JSON)
                .post()
                .then()
                .contentType(ContentType.JSON)
                .statusCode(400);
        assertEquals(emailCount, Email.count());
    }
}
