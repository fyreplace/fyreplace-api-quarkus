package app.fyreplace.api.testing.endpoints.subscriptions;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.Subscription;
import app.fyreplace.api.endpoints.SubscriptionsEndpoint;
import app.fyreplace.api.testing.SubscriptionTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(SubscriptionsEndpoint.class)
public final class DeleteTests extends SubscriptionTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void delete() {
        final var subscriptions =
                Subscription.<Subscription>list("user.username = 'user_0' and unreadCommentCount > 0");
        given().delete(subscriptions.getFirst().id.toString()).then().statusCode(204);
        assertEquals(
                subscriptions.size() - 1, Subscription.count("user.username = 'user_0' and unreadCommentCount > 0"));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void deleteOtherSubscription() {
        final var subscriptions =
                Subscription.<Subscription>list("user.username = 'user_0' and unreadCommentCount > 0");
        given().delete(subscriptions.getFirst().id.toString()).then().statusCode(404);
        assertEquals(subscriptions.size(), Subscription.count("user.username = 'user_0' and unreadCommentCount > 0"));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void deleteNonExistent() {
        final var subscriptionCount = Subscription.count("user.username = 'user_0' and unreadCommentCount > 0");
        given().delete(fakeId).then().statusCode(404);
        assertEquals(subscriptionCount, Subscription.count("user.username = 'user_0' and unreadCommentCount > 0"));
    }
}
