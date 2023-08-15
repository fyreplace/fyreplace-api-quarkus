package app.fyreplace.api.testing.endpoints.emails;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

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
public final class DeleteTests extends TransactionalTests {
    private Email newEmail;

    @Test
    @TestSecurity(user = "user_0")
    public void delete() {
        given().delete(newEmail.id.toString()).then().statusCode(204);
        assertEquals(0, Email.count("id", newEmail.id));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void deleteMainEmail() {
        final var user = User.findByUsername("user_0");
        final var emailCount = Email.count();
        given().delete(user.mainEmail.id.toString()).then().statusCode(403);
        assertEquals(emailCount, Email.count());
    }

    @Test
    @TestSecurity(user = "user_0")
    public void deleteOtherEmail() {
        final var otherUser = User.findByUsername("user_1");
        final var emailCount = Email.count();
        given().delete(otherUser.mainEmail.id.toString()).then().statusCode(404);
        assertEquals(emailCount, Email.count());
    }

    @Test
    @TestSecurity(user = "user_0")
    public void deleteNonExistentEmail() {
        final var emailCount = Email.count();
        given().delete(fakeId).then().statusCode(404);
        assertEquals(emailCount, Email.count());
    }

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        newEmail = new Email();
        newEmail.user = User.findByUsername("user_0");
        newEmail.email = "new_email@example.org";
        newEmail.verified = true;
        newEmail.persist();
    }
}
