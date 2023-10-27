package app.fyreplace.api.testing.endpoints.pushnotificationtokens;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.PushNotificationToken;
import app.fyreplace.api.data.PushNotificationTokenCreation;
import app.fyreplace.api.endpoints.PushNotificationTokensEndpoint;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(PushNotificationTokensEndpoint.class)
public final class UpdateTests extends UserTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void update() {
        final var tokenCount = PushNotificationToken.count();
        given().contentType(ContentType.JSON)
                .body(new PushNotificationTokenCreation(PushNotificationToken.Service.WEB, "token"))
                .put()
                .then()
                .statusCode(200);
        assertEquals(tokenCount + 1, PushNotificationToken.count());
        final var token = PushNotificationToken.<PushNotificationToken>find(
                        "user.username = 'user_0' and token = 'token'")
                .firstResult();
        assertEquals(PushNotificationToken.Service.WEB, token.service);
    }

    @Test
    @TestSecurity(user = "user_0")
    public void updateTwice() {
        final var tokenCount = PushNotificationToken.count();
        final var input = new PushNotificationTokenCreation(PushNotificationToken.Service.WEB, "token");
        given().contentType(ContentType.JSON).body(input).put().then().statusCode(200);
        given().contentType(ContentType.JSON).body(input).put().then().statusCode(200);
        assertEquals(tokenCount + 1, PushNotificationToken.count());
        final var token = PushNotificationToken.<PushNotificationToken>find(
                        "user.username = 'user_0' and token = 'token'")
                .firstResult();
        assertEquals(PushNotificationToken.Service.WEB, token.service);
    }
}
