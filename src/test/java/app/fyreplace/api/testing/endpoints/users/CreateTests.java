package app.fyreplace.api.testing.endpoints.users;

import static app.fyreplace.api.testing.Assertions.assertSingleEmail;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.User;
import app.fyreplace.api.data.UserCreation;
import app.fyreplace.api.emails.UserActivationEmail;
import app.fyreplace.api.endpoints.UsersEndpoint;
import app.fyreplace.api.testing.TransactionalTests;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(UsersEndpoint.class)
public final class CreateTests extends TransactionalTests {
    @Test
    public void create() {
        final var userCount = User.count();
        final var emailCount = Email.count();
        given().contentType(ContentType.JSON)
                .body(new UserCreation("new@example.org", "new_user"))
                .post()
                .then()
                .contentType(ContentType.JSON)
                .statusCode(201)
                .body("dateCreated", notNullValue())
                .body("username", equalTo("new_user"))
                .body("rank", equalTo(User.Rank.CITIZEN.name()))
                .body("banned", equalTo(false))
                .body("avatar", nullValue())
                .body("bio", equalTo(""))
                .body("banned", equalTo(false));
        assertEquals(userCount + 1, User.count());
        assertEquals(emailCount + 1, Email.count());
        final var user = User.findByUsername("new_user");
        assertNotNull(user);
        assertEquals("new@example.org", user.mainEmail.email);
        assertFalse(user.mainEmail.verified);
        final var mails = getMailsSentTo(user.mainEmail);
        assertSingleEmail(UserActivationEmail.class, mails);
    }

    @Test
    public void createWithInvalidUsername() {
        final var userCount = User.count();
        given().contentType(ContentType.JSON)
                .body(new UserCreation("new@example.org", "no spaces allowed"))
                .post()
                .then()
                .contentType(ContentType.JSON)
                .statusCode(400);
        assertEquals(userCount, User.count());
    }

    @Test
    public void createWithUsernameTooShort() {
        final var userCount = User.count();
        given().contentType(ContentType.JSON)
                .body(new UserCreation("new@example.org", "a"))
                .post()
                .then()
                .statusCode(400);
        assertEquals(userCount, User.count());
    }

    @Test
    public void createWithUsernameTooLong() {
        final var userCount = User.count();
        given().contentType(ContentType.JSON)
                .body(new UserCreation("new@example.org", "a".repeat(150)))
                .post()
                .then()
                .statusCode(400);
        assertEquals(userCount, User.count());
    }

    @Test
    public void createWithForbiddenUsername() {
        final var userCount = User.count();
        given().contentType(ContentType.JSON)
                .body(new UserCreation("new@example.org", "admin"))
                .post()
                .then()
                .statusCode(403);
        assertEquals(userCount, User.count());
    }

    @Test
    public void createWithInvalidEmail() {
        final var userCount = User.count();
        given().contentType(ContentType.JSON)
                .body(new UserCreation("not-an-email", "new_user"))
                .post()
                .then()
                .statusCode(400);
        assertEquals(userCount, User.count());
    }

    @Test
    public void createWithEmptyEmail() {
        final var userCount = User.count();
        given().contentType(ContentType.JSON)
                .body(new UserCreation("", "new_user"))
                .post()
                .then()
                .statusCode(400);
        assertEquals(userCount, User.count());
    }

    @Test
    public void createWithExistingUsername() {
        final var userCount = User.count();
        final var existingUser = User.<User>findAll().firstResult();
        given().contentType(ContentType.JSON)
                .body(new UserCreation("email@example.org", existingUser.username))
                .post()
                .then()
                .statusCode(409);
        assertEquals(userCount, User.count());
    }

    @Test
    public void createWithExistingEmail() {
        final var userCount = User.count();
        final var existingEmail = Email.<Email>findAll().firstResult();
        given().contentType(ContentType.JSON)
                .body(new UserCreation(existingEmail.email, "new_user"))
                .post()
                .then()
                .statusCode(409);
        assertEquals(userCount, User.count());
    }

    @Test
    public void createWithEmptyInput() {
        final var userCount = User.count();
        given().contentType(ContentType.JSON).post().then().statusCode(400);
        assertEquals(userCount, User.count());
    }
}
