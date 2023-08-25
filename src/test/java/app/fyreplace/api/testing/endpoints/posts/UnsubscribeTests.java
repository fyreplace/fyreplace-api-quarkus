package app.fyreplace.api.testing.endpoints.posts;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.Subscription;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.PostsEndpoint;
import app.fyreplace.api.testing.endpoints.PostTestsBase;
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
public final class UnsubscribeTests extends PostTestsBase {
    @Test
    @TestSecurity(user = "user_1")
    public void unsubscribeFromOtherPost() {
        final var user = User.findByUsername("user_1");
        assertEquals(1, Subscription.count("user = ?1 and post = ?2", user, post));
        given().delete(post.id + "/isSubscribed").then().statusCode(204);
        assertEquals(0, Subscription.count("user = ?1 and post = ?2", user, post));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void unsubscribeFromOtherPostTwice() {
        final var user = User.findByUsername("user_1");
        given().delete(post.id + "/isSubscribed").then().statusCode(204);
        given().delete(post.id + "/isSubscribed").then().statusCode(204);
        assertEquals(0, Subscription.count("user = ?1 and post = ?2", user, post));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void unsubscribeFromOwnPost() {
        final var user = User.findByUsername("user_0");
        assertEquals(1, Subscription.count("user = ?1 and post = ?2", user, post));
        given().delete(post.id + "/isSubscribed").then().statusCode(204);
        assertEquals(0, Subscription.count("user = ?1 and post = ?2", user, post));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void unsubscribeFromOtherDraft() {
        final var user = User.findByUsername("user_1");
        assertEquals(0, Subscription.count("user = ?1 and post = ?2", user, draft));
        given().delete(draft.id + "/isSubscribed").then().statusCode(404);
        assertEquals(0, Subscription.count("user = ?1 and post = ?2", user, draft));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void unsubscribeFromOwnDraft() {
        final var user = User.findByUsername("user_0");
        assertEquals(0, Subscription.count("user = ?1 and post = ?2", user, draft));
        given().delete(draft.id + "/isSubscribed").then().statusCode(403);
        assertEquals(0, Subscription.count("user = ?1 and post = ?2", user, draft));
    }

    @Test
    public void unsubscribeFromPostUnauthenticated() {
        final var subscriptionCount = Subscription.count();
        given().delete(post.id + "/isSubscribed").then().statusCode(401);
        assertEquals(subscriptionCount, Subscription.count());
    }

    @Test
    public void unsubscribeFromDraftUnauthenticated() {
        final var subscriptionCount = Subscription.count();
        given().delete(draft.id + "/isSubscribed").then().statusCode(401);
        assertEquals(subscriptionCount, Subscription.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"fake", "00000000-0000-0000-0000-000000000000"})
    @TestSecurity(user = "user_0")
    public void unsubscribeFromNonExistent(final String id) {
        final var subscriptionCount = Subscription.count();
        given().delete(id + "/isSubscribed").then().statusCode(404);
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
