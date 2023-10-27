package app.fyreplace.api.testing.endpoints.comments;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.Comment;
import app.fyreplace.api.data.CommentCreation;
import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.CommentsEndpoint;
import app.fyreplace.api.testing.CommentTestsBase;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(CommentsEndpoint.class)
public final class CreateTests extends CommentTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void createOnOwnPost() {
        final var commentCount = Comment.count("post", post);
        final var input = new CommentCreation("Text", false);
        final var response = given().contentType(ContentType.JSON)
                .body(input)
                .pathParam("id", post.id)
                .post()
                .then()
                .statusCode(201)
                .body("text", equalTo(input.text()))
                .body("anonymous", equalTo(input.anonymous()));
        assertEquals(commentCount + 1, Comment.count("post", post));
        final var comment = getLastComment(post);
        response.body("id", equalTo(comment.id.toString()));
        assertEquals(input.text(), comment.text);
        assertEquals("user_0", comment.author.username);
    }

    @Test
    @TestSecurity(user = "user_0")
    public void createAnonymouslyOnOwnPost() {
        final var commentCount = Comment.count("post", post);
        final var input = new CommentCreation("Text", true);
        final var response = given().contentType(ContentType.JSON)
                .body(input)
                .pathParam("id", post.id)
                .post()
                .then()
                .statusCode(201)
                .body("text", equalTo(input.text()))
                .body("anonymous", equalTo(input.anonymous()));
        assertEquals(commentCount + 1, Comment.count("post", post));
        final var comment = getLastComment(post);
        response.body("id", equalTo(comment.id.toString()));
        assertEquals(input.text(), comment.text);
        assertEquals("user_0", comment.author.username);
    }

    @Test
    @TestSecurity(user = "user_0")
    public void createOnOwnDraft() {
        final var input = new CommentCreation("Text", false);
        given().contentType(ContentType.JSON)
                .body(input)
                .pathParam("id", draft.id)
                .post()
                .then()
                .statusCode(403);
        assertEquals(0, Comment.count("post", draft));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void createOnOtherPost() {
        final var commentCount = Comment.count("post", post);
        final var input = new CommentCreation("Text", false);
        final var response = given().contentType(ContentType.JSON)
                .body(input)
                .pathParam("id", post.id)
                .post()
                .then()
                .statusCode(201)
                .body("text", equalTo(input.text()));
        assertEquals(commentCount + 1, Comment.count("post", post));
        final var comment = getLastComment(post);
        response.body("id", equalTo(comment.id.toString()));
        assertEquals(input.text(), comment.text);
        assertEquals("user_1", comment.author.username);
    }

    @Test
    @TestSecurity(user = "user_1")
    public void createAnonymouslyOnOtherPost() {
        final var commentCount = Comment.count("post", post);
        final var input = new CommentCreation("Text", true);
        given().contentType(ContentType.JSON)
                .body(input)
                .pathParam("id", post.id)
                .post()
                .then()
                .statusCode(403);
        assertEquals(commentCount, Comment.count("post", post));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void createOnOtherPostWhenBlocked() {
        QuarkusTransaction.requiringNew().run(() -> post.author.block(User.findByUsername("user_1")));
        final var commentCount = Comment.count("post", post);
        final var input = new CommentCreation("Text", false);
        given().contentType(ContentType.JSON)
                .body(input)
                .pathParam("id", post.id)
                .post()
                .then()
                .statusCode(403);
        assertEquals(commentCount, Comment.count("post", post));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void createOnOtherAnonymousPostWhenBlocked() {
        QuarkusTransaction.requiringNew().run(() -> anonymousPost.author.block(User.findByUsername("user_1")));
        final var commentCount = Comment.count("post", anonymousPost);
        final var input = new CommentCreation("Text", false);
        given().contentType(ContentType.JSON)
                .body(input)
                .pathParam("id", anonymousPost.id)
                .post()
                .then()
                .statusCode(201);
        assertEquals(commentCount + 1, Comment.count("post", anonymousPost));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void createOnOtherDraft() {
        final var input = new CommentCreation("Text", false);
        given().contentType(ContentType.JSON)
                .body(input)
                .pathParam("id", draft.id)
                .post()
                .then()
                .statusCode(404);
        assertEquals(0, Comment.count("post", draft));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void createOnNonExistentPost() {
        final var commentCount = Comment.count();
        final var input = new CommentCreation("Text", false);
        given().contentType(ContentType.JSON)
                .body(input)
                .pathParam("id", fakeId)
                .post()
                .then()
                .statusCode(404);
        assertEquals(commentCount, Comment.count());
    }

    @Test
    @TestSecurity(user = "user_1")
    public void createWithEmptyInput() {
        final var commentCount = Comment.count("post", post);
        given().contentType(ContentType.JSON)
                .pathParam("id", post.id)
                .post()
                .then()
                .statusCode(400);
        assertEquals(commentCount, Comment.count("post", post));
    }

    @Test
    public void createUnauthenticated() {
        final var commentCount = Comment.count("post", post);
        final var input = new CommentCreation("Text", false);
        given().contentType(ContentType.JSON)
                .body(input)
                .pathParam("id", post.id)
                .post()
                .then()
                .statusCode(401);
        assertEquals(commentCount, Comment.count("post", post));
    }

    private Comment getLastComment(final Post post) {
        return Comment.<Comment>find("post", Comment.sorting().descending(), post)
                .firstResult();
    }
}
