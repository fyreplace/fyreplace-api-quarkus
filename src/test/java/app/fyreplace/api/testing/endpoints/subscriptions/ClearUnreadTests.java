package app.fyreplace.api.testing.endpoints.subscriptions;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import app.fyreplace.api.data.Subscription;
import app.fyreplace.api.endpoints.SubscriptionsEndpoint;
import app.fyreplace.api.testing.SubscriptionTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(SubscriptionsEndpoint.class)
public class ClearUnreadTests extends SubscriptionTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void clearUnread() {
        assertNotEquals(0, Subscription.count("user.username = 'user_0' and unreadCommentCount > 0"));
        given().delete("unread").then().statusCode(204);
        assertEquals(0, Subscription.count("user.username = 'user_0' and unreadCommentCount > 0"));
    }
}
