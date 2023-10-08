package app.fyreplace.api.testing.endpoints.posts;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.fyreplace.api.data.Subscription;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.PostsEndpoint;
import app.fyreplace.api.testing.PostTestsBase;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestHTTPEndpoint(PostsEndpoint.class)
public final class DeleteSubscriptionTests extends PostTestsBase {
    @Test
    @TestSecurity(user = "user_1")
    public void deleteSubscriptionWithOtherPost() {
        final var user = requireNonNull(User.findByUsername("user_1"));
        assertTrue(user.isSubscribedTo(post));
        given().delete(post.id + "/subscribed").then().statusCode(204);
        assertFalse(user.isSubscribedTo(post));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void deleteSubscriptionWithOtherPostTwice() {
        final var user = requireNonNull(User.findByUsername("user_1"));
        given().delete(post.id + "/subscribed").then().statusCode(204);
        given().delete(post.id + "/subscribed").then().statusCode(204);
        assertFalse(user.isSubscribedTo(post));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void deleteSubscriptionWithOwnPost() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        assertTrue(user.isSubscribedTo(post));
        given().delete(post.id + "/subscribed").then().statusCode(204);
        assertFalse(user.isSubscribedTo(post));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void deleteSubscriptionWithOtherDraft() {
        final var user = requireNonNull(User.findByUsername("user_1"));
        assertFalse(user.isSubscribedTo(draft));
        given().delete(draft.id + "/subscribed").then().statusCode(404);
        assertFalse(user.isSubscribedTo(draft));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void deleteSubscriptionWithOwnDraft() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        assertFalse(user.isSubscribedTo(draft));
        given().delete(draft.id + "/subscribed").then().statusCode(403);
        assertFalse(user.isSubscribedTo(draft));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void deleteToOtherPostWhenBlocked() {
        final var user = requireNonNull(User.findByUsername("user_1"));
        QuarkusTransaction.requiringNew().run(() -> post.author.block(user));
        assertFalse(user.isSubscribedTo(post));
        given().delete(post.id + "/subscribed").then().statusCode(403);
        assertFalse(user.isSubscribedTo(post));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void deleteToOtherAnonymousPostWhenBlocked() {
        final var user = requireNonNull(User.findByUsername("user_1"));
        QuarkusTransaction.requiringNew().run(() -> {
            anonymousPost.author.block(user);
            user.subscribeTo(anonymousPost);
        });
        assertTrue(user.isSubscribedTo(anonymousPost));
        given().delete(anonymousPost.id + "/subscribed").then().statusCode(204);
        assertFalse(user.isSubscribedTo(anonymousPost));
    }

    @Test
    public void deleteSubscriptionWithPostUnauthenticated() {
        final var subscriptionCount = Subscription.count();
        given().delete(post.id + "/subscribed").then().statusCode(401);
        assertEquals(subscriptionCount, Subscription.count());
    }

    @Test
    public void deleteSubscriptionWithDraftUnauthenticated() {
        final var subscriptionCount = Subscription.count();
        given().delete(draft.id + "/subscribed").then().statusCode(401);
        assertEquals(subscriptionCount, Subscription.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"fake", "00000000-0000-0000-0000-000000000000"})
    @TestSecurity(user = "user_0")
    public void deleteSubscriptionWithNonExistent(final String id) {
        final var subscriptionCount = Subscription.count();
        given().delete(id + "/subscribed").then().statusCode(404);
        assertEquals(subscriptionCount, Subscription.count());
    }

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        requireNonNull(User.findByUsername("user_1")).subscribeTo(post);
    }
}
