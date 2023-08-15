package app.fyreplace.api.testing.endpoints.comments;

import static io.restassured.RestAssured.given;
import static java.util.stream.IntStream.range;
import static org.hamcrest.Matchers.equalTo;

import app.fyreplace.api.data.Comment;
import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.Subscription;
import app.fyreplace.api.data.User;
import app.fyreplace.api.data.dev.DataSeeder;
import app.fyreplace.api.endpoints.CommentsEndpoint;
import app.fyreplace.api.testing.endpoints.PostTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(CommentsEndpoint.class)
public class CountTests extends PostTestsBase {
    @Inject
    DataSeeder dataSeeder;

    private static final int readCommentCount = 10;

    private static final int notReadCommentCount = 6;

    @Test
    @TestSecurity(user = "user_0")
    public void count() {
        given().pathParam("id", post.id)
                .get("count")
                .then()
                .statusCode(200)
                .body(equalTo(String.valueOf(readCommentCount + notReadCommentCount)));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void countRead() {
        given().pathParam("id", post.id)
                .queryParam("read", true)
                .get("count")
                .then()
                .statusCode(200)
                .body(equalTo(String.valueOf(readCommentCount)));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void countNotRead() {
        given().pathParam("id", post.id)
                .queryParam("read", false)
                .get("count")
                .then()
                .statusCode(200)
                .body(equalTo(String.valueOf(notReadCommentCount)));
    }

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        Comment.deleteAll();
        final var user = User.findByUsername("user_1");
        range(0, readCommentCount).forEach(i -> dataSeeder.createComment(user, post, "Comment " + i, false));
        final var subscription = Subscription.<Subscription>find("user.username = 'user_0' and post = ?1", post)
                .firstResult();
        subscription.lastCommentSeen = Comment.<Comment>find(
                        "post", Comment.sorting().descending(), post)
                .firstResult();
        subscription.persist();
        range(0, notReadCommentCount).forEach(i -> dataSeeder.createComment(user, post, "Comment " + i, false));
        range(0, 10)
                .forEach(i -> dataSeeder.createComment(
                        user, Post.find("id != ?1", post.id).firstResult(), "Comment " + i, false));
    }
}
