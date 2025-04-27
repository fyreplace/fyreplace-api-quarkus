package app.fyreplace.api.testing.endpoints.emails;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.EmailVerification;
import app.fyreplace.api.data.RandomCode;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.EmailsEndpoint;
import app.fyreplace.api.services.RandomService;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.elytron.security.common.BcryptUtil;
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
public final class VerifyEmailTests extends UserTestsBase {
    @Inject
    RandomService randomService;

    private Email newEmail;
    private RandomCode randomCode;
    private String randomCodeClearText;

    @Test
    @TestSecurity(user = "user_0")
    public void verifyEmail() {
        given().contentType(ContentType.JSON)
                .body(new EmailVerification(newEmail.email, randomCodeClearText))
                .post("verify")
                .then()
                .statusCode(200);
        assertEquals(0, RandomCode.count("id", randomCode.id));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void verifyEmailWithInvalidEmail() {
        given().contentType(ContentType.JSON)
                .body(new EmailVerification("invalid", randomCodeClearText))
                .post("verify")
                .then()
                .statusCode(400);
        assertEquals(1, RandomCode.count("id", randomCode.id));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void verifyEmailWithOtherEmail() {
        final var otherUser = requireNonNull(User.findByUsername("user_1"));
        given().contentType(ContentType.JSON)
                .body(new EmailVerification(otherUser.mainEmail.email, randomCodeClearText))
                .post("verify")
                .then()
                .statusCode(404);
        assertEquals(1, RandomCode.count("id", randomCode.id));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void verifyEmailWithInvalidCode() {
        given().contentType(ContentType.JSON)
                .body(new EmailVerification(newEmail.email, "invalid"))
                .post("verify")
                .then()
                .statusCode(404);
        assertEquals(1, RandomCode.count("id", randomCode.id));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void verifyEmailWithEmptyInput() {
        given().contentType(ContentType.JSON).post("verify").then().statusCode(400);
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
        randomCodeClearText = randomService.generateCode(RandomCode.LENGTH);
        randomCode.code = BcryptUtil.bcryptHash(randomCodeClearText);
        randomCode.persist();
    }
}
