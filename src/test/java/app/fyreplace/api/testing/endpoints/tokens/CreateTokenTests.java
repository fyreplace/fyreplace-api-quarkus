package app.fyreplace.api.testing.endpoints.tokens;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.Password;
import app.fyreplace.api.data.RandomCode;
import app.fyreplace.api.data.TokenCreation;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.TokensEndpoint;
import app.fyreplace.api.services.RandomService;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(TokensEndpoint.class)
public final class CreateTokenTests extends UserTestsBase {
    @Inject
    RandomService randomService;

    private RandomCode normalUserRandomCode;
    private String normalUserRandomCodeClearText;
    private String otherNormalUserRandomCodeClearText;
    private RandomCode newUserRandomCode;
    private String newUserRandomCodeClearText;
    private Password password;

    @Test
    public void createTokenWithUsername() {
        final var randomCodeCount = RandomCode.count();
        given().contentType(ContentType.JSON)
                .body(new TokenCreation(normalUserRandomCode.email.user.username, normalUserRandomCodeClearText))
                .post()
                .then()
                .statusCode(201)
                .body(isA(String.class));
        assertEquals(randomCodeCount - 1, RandomCode.count());
    }

    @Test
    @Transactional
    public void createTokenWithNewUsername() {
        final var randomCodeCount = RandomCode.count();
        assertFalse(newUserRandomCode.email.verified);
        given().contentType(ContentType.JSON)
                .body(new TokenCreation(newUserRandomCode.email.user.username, newUserRandomCodeClearText))
                .post()
                .then()
                .statusCode(201)
                .contentType(ContentType.TEXT)
                .body(isA(String.class));
        assertEquals(randomCodeCount - 1, RandomCode.count());
        final var email = Email.<Email>find("id", newUserRandomCode.email.id).firstResult();
        assertTrue(email.verified);
    }

    @Test
    public void createTokenWithEmail() {
        final var randomCodeCount = RandomCode.count();
        given().contentType(ContentType.JSON)
                .body(new TokenCreation(normalUserRandomCode.email.email, normalUserRandomCodeClearText))
                .post()
                .then()
                .statusCode(201)
                .contentType(ContentType.TEXT)
                .body(isA(String.class));
        assertEquals(randomCodeCount - 1, RandomCode.count());
    }

    @Test
    public void createTokenWithNewEmail() {
        final var randomCodeCount = RandomCode.count();
        assertFalse(newUserRandomCode.email.verified);
        given().contentType(ContentType.JSON)
                .body(new TokenCreation(newUserRandomCode.email.email, newUserRandomCodeClearText))
                .post()
                .then()
                .statusCode(201)
                .contentType(ContentType.TEXT)
                .body(isA(String.class));
        assertEquals(randomCodeCount - 1, RandomCode.count());
        final var email = Email.<Email>find("id", newUserRandomCode.email.id).firstResult();
        assertTrue(email.verified);
    }

    @Test
    public void createTokenWithPassword() {
        final var randomCodeCount = RandomCode.count();
        assertFalse(password.user.mainEmail.verified);
        given().contentType(ContentType.JSON)
                .body(new TokenCreation(password.user.username, "password"))
                .post()
                .then()
                .statusCode(201)
                .contentType(ContentType.TEXT)
                .body(isA(String.class));
        assertEquals(randomCodeCount, RandomCode.count());
    }

    @Test
    public void createTokenWithInvalidUsername() {
        given().contentType(ContentType.JSON)
                .body(new TokenCreation("bad", normalUserRandomCodeClearText))
                .post()
                .then()
                .statusCode(404);
    }

    @Test
    public void createTokenWithInvalidSecret() {
        given().contentType(ContentType.JSON)
                .body(new TokenCreation(normalUserRandomCode.email.user.username, "bad"))
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    public void createTokenTwice() {
        final var input = new TokenCreation(normalUserRandomCode.email.user.username, normalUserRandomCodeClearText);
        given().contentType(ContentType.JSON).body(input).post().then().statusCode(201);
        given().contentType(ContentType.JSON).body(input).post().then().statusCode(400);
    }

    @Test
    public void createTokenWithOtherCode() {
        given().contentType(ContentType.JSON)
                .body(new TokenCreation(normalUserRandomCode.email.user.username, otherNormalUserRandomCodeClearText))
                .post()
                .then()
                .statusCode(400);
    }

    @Test
    public void createTokenWithEmptyInput() {
        given().contentType(ContentType.JSON).post().then().statusCode(400);
    }

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        normalUserRandomCodeClearText = generateCode();
        normalUserRandomCode = makeRandomCode("user_0", normalUserRandomCodeClearText);
        otherNormalUserRandomCodeClearText = generateCode();
        makeRandomCode("user_1", otherNormalUserRandomCodeClearText);
        newUserRandomCodeClearText = generateCode();
        newUserRandomCode = makeRandomCode("user_inactive_0", newUserRandomCodeClearText);
        password = new Password();
        password.user = User.findByUsername("user_inactive_1");
        password.password = BcryptUtil.bcryptHash("password");
        password.persist();
        Email.delete("user", password.user);
    }

    private RandomCode makeRandomCode(final String username, final String code) {
        final var randomCode = new RandomCode();
        randomCode.email = requireNonNull(User.findByUsername(username)).mainEmail;
        randomCode.code = BcryptUtil.bcryptHash(code);
        randomCode.persist();
        return randomCode;
    }

    private String generateCode() {
        return randomService.generateCode(RandomCode.LENGTH);
    }
}
