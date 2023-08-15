package app.fyreplace.api.testing.endpoints.posts;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import app.fyreplace.api.data.Comment;
import app.fyreplace.api.data.Subscription;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.PostsEndpoint;
import app.fyreplace.api.testing.endpoints.PostTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestHTTPEndpoint(PostsEndpoint.class)
public final class SubscribeTests extends PostTestsBase {
    @Test
    @TestSecurity(user = "user_1")
    public void subscribeToOtherPost() {
        final var user = User.findByUsername("user_1");
        assertEquals(0, Subscription.count("user = ?1 and post = ?2", user, post));
        given().put(post.id + "/isSubscribed").then().statusCode(200);
        final var subscription = Subscription.<Subscription>find("user = ?1 and post = ?2", user, post)
                .firstResult();
        assertNotNull(subscription);
        final var comment = Comment.<Comment>find("post", Comment.sorting().descending(), post)
                .firstResult();
        assertEquals(comment.id, subscription.lastCommentSeen.id);
    }

    @Test
    @TestSecurity(user = "user_1")
    public void subscribeToOtherPostTwice() {
        final var user = User.findByUsername("user_1");
        given().put(post.id + "/isSubscribed").then().statusCode(200);
        given().put(post.id + "/isSubscribed").then().statusCode(200);
        final var subscription = Subscription.<Subscription>find("user = ?1 and post = ?2", user, post)
                .firstResult();
        assertNotNull(subscription);
        final var comment = Comment.<Comment>find("post", Comment.sorting().descending(), post)
                .firstResult();
        assertEquals(comment.id, subscription.lastCommentSeen.id);
    }

    @Test
    @TestSecurity(user = "user_0")
    public void subscribeToOwnPost() {
        final var user = User.findByUsername("user_0");
        assertEquals(1, Subscription.count("user = ?1 and post = ?2", user, post));
        given().put(post.id + "/isSubscribed").then().statusCode(200);
        final var subscription = Subscription.<Subscription>find("user = ?1 and post = ?2", user, post)
                .firstResult();
        assertNotNull(subscription);
        assertNull(subscription.lastCommentSeen);
    }

    @Test
    @TestSecurity(user = "user_1")
    public void subscribeToOtherDraft() {
        final var user = User.findByUsername("user_1");
        assertEquals(0, Subscription.count("user = ?1 and post = ?2", user, draft));
        given().put(draft.id + "/isSubscribed").then().statusCode(404);
        assertEquals(0, Subscription.count("user = ?1 and post = ?2", user, draft));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void subscribeToOwnDraft() {
        final var user = User.findByUsername("user_0");
        assertEquals(0, Subscription.count("user = ?1 and post = ?2", user, draft));
        given().put(draft.id + "/isSubscribed").then().statusCode(403);
        assertEquals(0, Subscription.count("user = ?1 and post = ?2", user, draft));
    }

    @Test
    public void subscribeToPostUnauthenticated() {
        final var subscriptionCount = Subscription.count();
        given().put(post.id + "/isSubscribed").then().statusCode(401);
        assertEquals(subscriptionCount, Subscription.count());
    }

    @Test
    public void subscribeToDraftUnauthenticated() {
        final var subscriptionCount = Subscription.count();
        given().put(draft.id + "/isSubscribed").then().statusCode(401);
        assertEquals(subscriptionCount, Subscription.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"fake", "00000000-0000-0000-0000-000000000000"})
    @TestSecurity(user = "user_0")
    public void subscribeToNonExistent(final String id) {
        final var subscriptionCount = Subscription.count();
        given().put(id + "/isSubscribed").then().statusCode(404);
        assertEquals(subscriptionCount, Subscription.count());
    }
}
