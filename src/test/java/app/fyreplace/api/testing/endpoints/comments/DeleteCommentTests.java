package app.fyreplace.api.testing.endpoints.comments;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.fyreplace.api.data.Comment;
import app.fyreplace.api.data.User;
import app.fyreplace.api.data.dev.DataSeeder;
import app.fyreplace.api.endpoints.CommentsEndpoint;
import app.fyreplace.api.testing.CommentTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(CommentsEndpoint.class)
public final class DeleteCommentTests extends CommentTestsBase {
    @Inject
    DataSeeder dataSeeder;

    @Test
    @TestSecurity(user = "user_0")
    @Transactional
    public void deleteOwnCommentOnOwnPost() {
        final var commentCount = Comment.count("post", post);
        final var position = 0;
        given().pathParam("id", post.id).delete(String.valueOf(position)).then().statusCode(204);
        assertEquals(commentCount, Comment.count("post", post));
        final var comment = getComment(position);
        comment.refresh();
        assertTrue(comment.deleted);
        assertEquals("", comment.text);
    }

    @Test
    @TestSecurity(user = "user_1")
    @Transactional
    public void deleteOwnCommentOnOtherPost() {
        final var commentCount = Comment.count("post", post);
        final var position = 1;
        given().pathParam("id", post.id).delete(String.valueOf(position)).then().statusCode(204);
        assertEquals(commentCount, Comment.count("post", post));
        final var comment = getComment(position);
        comment.refresh();
        assertTrue(comment.deleted);
        assertEquals("", comment.text);
    }

    @Test
    @TestSecurity(user = "user_0")
    @Transactional
    public void deleteOtherCommentOnOwnPost() {
        final var commentCount = Comment.count("post", post);
        final var position = 1;
        given().pathParam("id", post.id).delete(String.valueOf(position)).then().statusCode(403);
        assertEquals(commentCount, Comment.count("post", post));
        final var comment = getComment(position);
        comment.refresh();
        assertFalse(comment.deleted);
    }

    @Test
    @TestSecurity(user = "user_1")
    @Transactional
    public void deleteOtherCommentOnOtherPost() {
        final var commentCount = Comment.count("post", post);
        final var position = 0;
        given().pathParam("id", post.id).delete(String.valueOf(position)).then().statusCode(403);
        assertEquals(commentCount, Comment.count("post", post));
        final var comment = getComment(position);
        comment.refresh();
        assertFalse(comment.deleted);
    }

    @Test
    @TestSecurity(user = "user_1")
    public void deleteCommentOutOfBounds() {
        final var commentCount = Comment.count("post", post);
        final var position = -1;
        given().pathParam("id", post.id).delete(String.valueOf(position)).then().statusCode(400);
        assertEquals(commentCount, Comment.count("post", post));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void deleteCommentTooFar() {
        final var commentCount = Comment.count("post", post);
        final var position = 50;
        given().pathParam("id", post.id).delete(String.valueOf(position)).then().statusCode(404);
        assertEquals(commentCount, Comment.count("post", post));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void deleteCommentOnNonExistentPost() {
        final var commentCount = Comment.count();
        given().pathParam("id", fakeId).delete(String.valueOf(0)).then().statusCode(404);
        assertEquals(commentCount, Comment.count());
    }

    @Test
    public void deleteCommentWhileUnauthenticated() {
        final var commentCount = Comment.count("post", post);
        given().pathParam("id", post.id).delete(String.valueOf(0)).then().statusCode(401);
        assertEquals(commentCount, Comment.count("post", post));
    }

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        Comment.deleteAll();
        final var user = User.findByUsername("user_0");
        final var otherUser = User.findByUsername("user_1");
        dataSeeder.createComment(user, post, "Comment from user 0", false);
        dataSeeder.createComment(otherUser, post, "Comment from user 1", false);
    }

    private Comment getComment(final int position) {
        return Comment.<Comment>find("post", Comment.sorting(), post)
                .range(position, position)
                .firstResult();
    }
}
