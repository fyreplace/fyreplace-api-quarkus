package app.fyreplace.api.testing.endpoints.subscriptions;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.Subscription;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.SubscriptionsEndpoint;
import app.fyreplace.api.testing.SubscriptionTestsBase;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(SubscriptionsEndpoint.class)
public final class DeleteSubscriptionTests extends SubscriptionTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void deleteSubscription() {
        final var subscriptions =
                Subscription.<Subscription>list("user.username = 'user_0' and unreadCommentCount > 0");
        given().delete(subscriptions.getFirst().id.toString()).then().statusCode(204);
        assertEquals(
                subscriptions.size() - 1, Subscription.count("user.username = 'user_0' and unreadCommentCount > 0"));
    }

    @Test
    public void deletePost() {
        final var subscriptions =
                Subscription.<Subscription>list("user.username = 'user_0' and unreadCommentCount > 0");
        QuarkusTransaction.requiringNew()
                .run(() -> Post.<Post>findById(subscriptions.getFirst().post.id).softDelete());
        assertEquals(
                0, Subscription.count("post.id = ?1 and unreadCommentCount > 0", subscriptions.getFirst().post.id));
    }

    @Test
    public void deleteUser() {
        final var subscriptions =
                Subscription.<Subscription>list("user.username = 'user_0' and unreadCommentCount > 0");
        QuarkusTransaction.requiringNew()
                .run(() -> User.<User>findById(subscriptions.getFirst().user.id).softDelete());
        assertEquals(0, Subscription.count("user.username = 'user_0' and unreadCommentCount > 0"));
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
    public void deleteNonExistentSubscription() {
        final var subscriptionCount = Subscription.count("user.username = 'user_0' and unreadCommentCount > 0");
        given().delete(fakeId).then().statusCode(404);
        assertEquals(subscriptionCount, Subscription.count("user.username = 'user_0' and unreadCommentCount > 0"));
    }
}
