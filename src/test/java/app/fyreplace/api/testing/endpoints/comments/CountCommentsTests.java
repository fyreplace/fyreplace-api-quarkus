package app.fyreplace.api.testing.endpoints.comments;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.range;
import static org.hamcrest.Matchers.equalTo;

import app.fyreplace.api.data.Comment;
import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.Subscription;
import app.fyreplace.api.data.User;
import app.fyreplace.api.data.dev.DataSeeder;
import app.fyreplace.api.endpoints.CommentsEndpoint;
import app.fyreplace.api.testing.CommentTestsBase;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(CommentsEndpoint.class)
public final class CountCommentsTests extends CommentTestsBase {
    @Inject
    DataSeeder dataSeeder;

    private static final int READ_COMMENT_COUNT = 10;

    private static final int NOT_READ_COMMENT_COUNT = 6;

    @Test
    @TestSecurity(user = "user_1")
    public void countComments() {
        given().pathParam("id", post.id)
                .get("count")
                .then()
                .statusCode(200)
                .body(equalTo(String.valueOf(READ_COMMENT_COUNT + NOT_READ_COMMENT_COUNT)));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void countReadComments() {
        given().pathParam("id", post.id)
                .queryParam("read", true)
                .get("count")
                .then()
                .statusCode(200)
                .body(equalTo(String.valueOf(READ_COMMENT_COUNT)));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void countNotReadComments() {
        given().pathParam("id", post.id)
                .queryParam("read", false)
                .get("count")
                .then()
                .statusCode(200)
                .body(equalTo(String.valueOf(NOT_READ_COMMENT_COUNT)));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void countCommentsWhenBlocked() {
        QuarkusTransaction.requiringNew().run(() -> {
            final var user = requireNonNull(User.findByUsername("user_0"));
            final var otherUser = requireNonNull(User.findByUsername("user_1"));
            user.block(otherUser);
        });

        given().pathParam("id", post.id).get("count").then().statusCode(403);
    }

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        Comment.deleteAll();
        final var user1 = User.findByUsername("user_1");
        final var user2 = User.findByUsername("user_2");
        range(0, READ_COMMENT_COUNT).forEach(i -> dataSeeder.createComment(user2, post, "Comment " + i, false));
        requireNonNull(user1).subscribeTo(post);
        final var subscription = Subscription.<Subscription>find("user = ?1 and post = ?2", user1, post)
                .firstResult();
        subscription.lastCommentSeen = Comment.<Comment>find(
                        "post", Comment.sorting().descending(), post)
                .firstResult();
        subscription.persist();
        range(0, NOT_READ_COMMENT_COUNT).forEach(i -> dataSeeder.createComment(user2, post, "Comment " + i, false));
        range(0, 10)
                .forEach(i -> dataSeeder.createComment(
                        user2, Post.find("id != ?1", post.id).firstResult(), "Comment " + i, false));
    }
}
