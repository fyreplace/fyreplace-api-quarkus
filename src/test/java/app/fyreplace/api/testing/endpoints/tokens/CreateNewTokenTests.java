package app.fyreplace.api.testing.endpoints.tokens;

import static app.fyreplace.api.testing.Assertions.assertSingleEmail;
import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.NewTokenCreation;
import app.fyreplace.api.data.Password;
import app.fyreplace.api.data.User;
import app.fyreplace.api.emails.UserConnectionEmail;
import app.fyreplace.api.endpoints.TokensEndpoint;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(TokensEndpoint.class)
public final class CreateNewTokenTests extends UserTestsBase {
    @Test
    public void createNewTokenWithUsername() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        given().contentType(ContentType.JSON)
                .body(new NewTokenCreation(user.username))
                .post("new")
                .then()
                .statusCode(200);
        assertSingleEmail(UserConnectionEmail.class, getMailsSentTo(user.mainEmail));
    }

    @Test
    public void createNewTokenWithEmail() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        given().contentType(ContentType.JSON)
                .body(new NewTokenCreation(user.mainEmail.email))
                .post("new")
                .then()
                .statusCode(200);
        assertSingleEmail(UserConnectionEmail.class, getMailsSentTo(user.mainEmail));
    }

    @Test
    public void createNewTokenWhileUserHasPassword() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        QuarkusTransaction.requiringNew().run(() -> {
            final var password = new Password();
            password.user = user;
            password.password = BcryptUtil.bcryptHash("password");
            password.persist();
            Email.delete("user", user);
        });
        given().contentType(ContentType.JSON)
                .body(new NewTokenCreation(user.username))
                .post("new")
                .then()
                .statusCode(403);
        assertEquals(0, getMailsSentTo(user.mainEmail).size());
    }

    @Test
    public void createNewTokenWithInvalidIdentifier() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        given().contentType(ContentType.JSON)
                .body(new NewTokenCreation("invalid"))
                .post("new")
                .then()
                .statusCode(404);
        assertEquals(0, getMailsSentTo(user.mainEmail).size());
    }

    @Test
    public void createNewTokenWithEmptyInput() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        given().contentType(ContentType.JSON).post("new").then().statusCode(400);
        assertEquals(0, getMailsSentTo(user.mainEmail).size());
    }
}
