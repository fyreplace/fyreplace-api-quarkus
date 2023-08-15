package app.fyreplace.api.testing.endpoints.tokens;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.RandomCode;
import app.fyreplace.api.data.TokenCreation;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.TokensEndpoint;
import app.fyreplace.api.services.RandomService;
import app.fyreplace.api.testing.TransactionalTests;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(TokensEndpoint.class)
public final class CreateTests extends TransactionalTests {
    @Inject
    RandomService randomService;

    private RandomCode normalUserRandomCode;
    private RandomCode otherNormalUserRandomCode;
    private RandomCode newUserRandomCode;

    @Test
    public void createWithUsername() {
        final var randomCodeCount = RandomCode.count();
        given().contentType(ContentType.JSON)
                .body(new TokenCreation(normalUserRandomCode.email.user.username, normalUserRandomCode.code))
                .post()
                .then()
                .statusCode(201)
                .body(isA(String.class));
        assertEquals(randomCodeCount - 1, RandomCode.count());
    }

    @Test
    @Transactional
    public void createWithNewUsername() {
        final var randomCodeCount = RandomCode.count();
        assertFalse(newUserRandomCode.email.verified);
        given().contentType(ContentType.JSON)
                .body(new TokenCreation(newUserRandomCode.email.user.username, newUserRandomCode.code))
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
    public void createWithEmail() {
        final var randomCodeCount = RandomCode.count();
        given().contentType(ContentType.JSON)
                .body(new TokenCreation(normalUserRandomCode.email.email, normalUserRandomCode.code))
                .post()
                .then()
                .statusCode(201)
                .contentType(ContentType.TEXT)
                .body(isA(String.class));
        assertEquals(randomCodeCount - 1, RandomCode.count());
    }

    @Test
    public void createWithNewEmail() {
        final var randomCodeCount = RandomCode.count();
        assertFalse(newUserRandomCode.email.verified);
        given().contentType(ContentType.JSON)
                .body(new TokenCreation(newUserRandomCode.email.email, newUserRandomCode.code))
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
    public void createWithInvalidUsername() {
        given().contentType(ContentType.JSON)
                .body(new TokenCreation("bad", normalUserRandomCode.code))
                .post()
                .then()
                .statusCode(404);
    }

    @Test
    public void createWithInvalidCode() {
        given().contentType(ContentType.JSON)
                .body(new TokenCreation(normalUserRandomCode.email.user.username, "bad"))
                .post()
                .then()
                .statusCode(404);
    }

    @Test
    public void createTwice() {
        final var input = new TokenCreation(normalUserRandomCode.email.user.username, normalUserRandomCode.code);
        given().contentType(ContentType.JSON).body(input).post().then().statusCode(201);
        given().contentType(ContentType.JSON).body(input).post().then().statusCode(404);
    }

    @Test
    public void createWithOtherCode() {
        given().contentType(ContentType.JSON)
                .body(new TokenCreation(normalUserRandomCode.email.user.username, otherNormalUserRandomCode.code))
                .post()
                .then()
                .statusCode(404);
    }

    @Test
    public void createWithEmptyInput() {
        given().contentType(ContentType.JSON).post().then().statusCode(400);
    }

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        normalUserRandomCode = makeRandomCode("user_0");
        otherNormalUserRandomCode = makeRandomCode("user_1");
        newUserRandomCode = makeRandomCode("user_inactive_0");
    }

    private RandomCode makeRandomCode(final String username) {
        final var code = new RandomCode();
        code.email = User.findByUsername(username).mainEmail;
        code.code = randomService.generateCode();
        code.persist();
        return code;
    }
}
