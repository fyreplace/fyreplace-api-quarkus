package app.fyreplace.api.testing.endpoints.emails;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.EmailsEndpoint;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(EmailsEndpoint.class)
public final class UpdateMainTests extends UserTestsBase {
    private Email secondaryEmail;

    @Test
    @TestSecurity(user = "user_0")
    public void setMain() {
        assertFalse(secondaryEmail.isMain());
        given().put(secondaryEmail.id + "/main").then().statusCode(200);
        secondaryEmail = Email.findById(secondaryEmail.id);
        assertTrue(secondaryEmail.isMain());
    }

    @Test
    @TestSecurity(user = "user_0")
    public void setMainTwice() {
        assertFalse(secondaryEmail.isMain());
        given().put(secondaryEmail.id + "/main").then().statusCode(200);
        given().put(secondaryEmail.id + "/main").then().statusCode(200);
        secondaryEmail = Email.findById(secondaryEmail.id);
        assertTrue(secondaryEmail.isMain());
    }

    @Test
    @TestSecurity(user = "user_0")
    public void setMainWithUnverifiedEmail() {
        QuarkusTransaction.requiringNew().run(() -> Email.update("verified = false where id = ?1", secondaryEmail.id));
        given().put(secondaryEmail.id + "/main").then().statusCode(403);
        secondaryEmail = Email.findById(secondaryEmail.id);
        assertFalse(secondaryEmail.isMain());
    }

    @Test
    @TestSecurity(user = "user_0")
    public void setMainWithOtherEmail() {
        final var otherUser = requireNonNull(User.findByUsername("user_1"));
        given().put(otherUser.mainEmail.id + "/main").then().statusCode(404);
        secondaryEmail = Email.findById(secondaryEmail.id);
        assertFalse(secondaryEmail.isMain());
    }

    @Test
    @TestSecurity(user = "user_0")
    public void setMainWithNonExistentEmail() {
        given().put(fakeId + "/main").then().statusCode(404);
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
        secondaryEmail.verified = true;
        secondaryEmail.persist();
    }
}
