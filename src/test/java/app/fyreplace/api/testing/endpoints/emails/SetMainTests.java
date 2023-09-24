package app.fyreplace.api.testing.endpoints.emails;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.EmailsEndpoint;
import app.fyreplace.api.testing.TransactionalTests;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(EmailsEndpoint.class)
public final class SetMainTests extends TransactionalTests {
    private Email secondaryEmail;
    private Email unverifiedEmail;

    @Test
    @TestSecurity(user = "user_0")
    public void setMain() {
        assertFalse(secondaryEmail.isMain());
        given().post(secondaryEmail.id + "/isMain").then().statusCode(200);
        secondaryEmail = Email.findById(secondaryEmail.id);
        assertTrue(secondaryEmail.isMain());
    }

    @Test
    @TestSecurity(user = "user_0")
    public void setMainTwice() {
        assertFalse(secondaryEmail.isMain());
        given().post(secondaryEmail.id + "/isMain").then().statusCode(200);
        given().post(secondaryEmail.id + "/isMain").then().statusCode(200);
        secondaryEmail = Email.findById(secondaryEmail.id);
        assertTrue(secondaryEmail.isMain());
    }

    @Test
    @TestSecurity(user = "user_0")
    public void setMainWithUnverifiedEmail() {
        given().post(unverifiedEmail.id + "/isMain").then().statusCode(403);
        secondaryEmail = Email.findById(secondaryEmail.id);
        assertFalse(secondaryEmail.isMain());
    }

    @Test
    @TestSecurity(user = "user_0")
    public void setMainWithOtherEmail() {
        final var otherUser = User.findByUsername("user_1");
        given().post(otherUser.mainEmail.id + "/isMain").then().statusCode(404);
        secondaryEmail = Email.findById(secondaryEmail.id);
        assertFalse(secondaryEmail.isMain());
    }

    @Test
    @TestSecurity(user = "user_0")
    public void setMainWithNonExistentEmail() {
        given().post("invalid" + "/isMain").then().statusCode(404);
        secondaryEmail = Email.findById(secondaryEmail.id);
        assertFalse(secondaryEmail.isMain());
    }

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        secondaryEmail = new Email();
        secondaryEmail.user = User.findByUsername("user_0");
        secondaryEmail.email = "new_email@example.org";
        secondaryEmail.isVerified = true;
        secondaryEmail.persist();
        unverifiedEmail = new Email();
        unverifiedEmail.user = secondaryEmail.user;
        unverifiedEmail.email = "unverified@example.org";
        unverifiedEmail.isVerified = false;
        unverifiedEmail.persist();
    }
}
