package app.fyreplace.api.testing.endpoints.comments;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.Comment;
import app.fyreplace.api.data.Subscription;
import app.fyreplace.api.endpoints.CommentsEndpoint;
import app.fyreplace.api.testing.CommentTestsBase;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(CommentsEndpoint.class)
public class AcknowledgeTests extends CommentTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void acknowledge() {
        final var position = 5;
        given().pathParam("id", post.id).post(position + "/acknowledge").then().statusCode(200);
        final var subscription = Subscription.<Subscription>find("user.username = 'user_0' and post = ?1", post)
                .firstResult();
        final var comment =
                Comment.<Comment>list("post", Comment.sorting(), post).get(position);
        assertEquals(subscription.lastCommentSeen.id, comment.id);
    }

    @Test
    @TestSecurity(user = "user_0")
    public void acknowledgePastComment() {
        final var currentPosition = 5;
        final var position = 3;
        QuarkusTransaction.requiringNew().run(() -> {
            final var subscription = Subscription.<Subscription>find("user.username = 'user_0' and post = ?1", post)
                    .firstResult();
            subscription.lastCommentSeen =
                    Comment.<Comment>list("post", Comment.sorting(), post).get(currentPosition);
            subscription.persist();
        });
        given().pathParam("id", post.id).post(position + "/acknowledge").then().statusCode(200);
        final var subscription = Subscription.<Subscription>find("user.username = 'user_0' and post = ?1", post)
                .firstResult();
        final var comment =
                Comment.<Comment>list("post", Comment.sorting(), post).get(currentPosition);
        assertEquals(subscription.lastCommentSeen.id, comment.id);
    }

    @Test
    @TestSecurity(user = "user_0")
    public void acknowledgeOutOfBounds() {
        final var position = -1;
        given().pathParam("id", post.id).post(position + "/acknowledge").then().statusCode(400);
    }

    @Test
    @TestSecurity(user = "user_0")
    public void acknowledgeTooFar() {
        final var position = 12;
        given().pathParam("id", post.id).post(position + "/acknowledge").then().statusCode(404);
    }
}
