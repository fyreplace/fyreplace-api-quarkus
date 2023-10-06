package app.fyreplace.api.testing.endpoints.posts;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.fyreplace.api.data.Comment;
import app.fyreplace.api.data.Subscription;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.PostsEndpoint;
import app.fyreplace.api.testing.endpoints.PostTestsBase;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestHTTPEndpoint(PostsEndpoint.class)
public final class CreateSubscriptionTests extends PostTestsBase {
    @Test
    @TestSecurity(user = "user_1")
    public void createSubscriptionWithOtherPost() {
        final var user = requireNonNull(User.findByUsername("user_1"));
        assertFalse(user.isSubscribedTo(post));
        given().put(post.id + "/subscribed").then().statusCode(200);
        final var subscription = Subscription.<Subscription>find("user = ?1 and post = ?2", user, post)
                .firstResult();
        assertNotNull(subscription);
        final var comment = Comment.<Comment>find("post", Comment.sorting().descending(), post)
                .firstResult();
        assertEquals(comment.id, subscription.lastCommentSeen.id);
    }

    @Test
    @TestSecurity(user = "user_1")
    public void createSubscriptionWithOtherPostTwice() {
        final var user = User.findByUsername("user_1");
        given().put(post.id + "/subscribed").then().statusCode(200);
        given().put(post.id + "/subscribed").then().statusCode(200);
        final var subscription = Subscription.<Subscription>find("user = ?1 and post = ?2", user, post)
                .firstResult();
        assertNotNull(subscription);
        final var comment = Comment.<Comment>find("post", Comment.sorting().descending(), post)
                .firstResult();
        assertEquals(comment.id, subscription.lastCommentSeen.id);
    }

    @Test
    @TestSecurity(user = "user_0")
    public void createSubscriptionWithOwnPost() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        assertTrue(user.isSubscribedTo(post));
        given().put(post.id + "/subscribed").then().statusCode(200);
        final var subscription = Subscription.<Subscription>find("user = ?1 and post = ?2", user, post)
                .firstResult();
        assertNotNull(subscription);
        assertNull(subscription.lastCommentSeen);
    }

    @Test
    @TestSecurity(user = "user_1")
    public void createSubscriptionWithOtherDraft() {
        final var user = requireNonNull(User.findByUsername("user_1"));
        assertFalse(user.isSubscribedTo(draft));
        given().put(draft.id + "/subscribed").then().statusCode(404);
        assertFalse(user.isSubscribedTo(draft));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void createSubscriptionWithOwnDraft() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        assertFalse(user.isSubscribedTo(draft));
        given().put(draft.id + "/subscribed").then().statusCode(403);
        assertFalse(user.isSubscribedTo(draft));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void createSubscriptionWithOtherPostWhenBlocked() {
        final var user = requireNonNull(User.findByUsername("user_1"));
        QuarkusTransaction.requiringNew().run(() -> post.author.block(user));
        assertFalse(user.isSubscribedTo(post));
        given().put(post.id + "/subscribed").then().statusCode(403);
        assertFalse(user.isSubscribedTo(post));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void createSubscriptionWithOtherAnonymousPostWhenBlocked() {
        final var user = requireNonNull(User.findByUsername("user_1"));
        QuarkusTransaction.requiringNew().run(() -> anonymousPost.author.block(user));
        assertFalse(user.isSubscribedTo(anonymousPost));
        given().put(anonymousPost.id + "/subscribed").then().statusCode(200);
        assertTrue(user.isSubscribedTo(anonymousPost));
    }

    @Test
    public void createSubscriptionWithPostUnauthenticated() {
        final var subscriptionCount = Subscription.count();
        given().put(post.id + "/subscribed").then().statusCode(401);
        assertEquals(subscriptionCount, Subscription.count());
    }

    @Test
    public void createSubscriptionWithDraftUnauthenticated() {
        final var subscriptionCount = Subscription.count();
        given().put(draft.id + "/subscribed").then().statusCode(401);
        assertEquals(subscriptionCount, Subscription.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"fake", "00000000-0000-0000-0000-000000000000"})
    @TestSecurity(user = "user_0")
    public void createSubscriptionWithNonExistent(final String id) {
        final var subscriptionCount = Subscription.count();
        given().put(id + "/subscribed").then().statusCode(404);
        assertEquals(subscriptionCount, Subscription.count());
    }
}
