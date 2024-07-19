package app.fyreplace.api.testing.endpoints.posts;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

import app.fyreplace.api.data.Comment;
import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.User;
import app.fyreplace.api.data.Vote;
import app.fyreplace.api.endpoints.PostsEndpoint;
import app.fyreplace.api.testing.PostTestsBase;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestHTTPEndpoint(PostsEndpoint.class)
public final class GetPostTests extends PostTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void getOwnPost() {
        given().get(post.id.toString())
                .then()
                .statusCode(200)
                .body("id", equalTo(post.id.toString()))
                .body("dateCreated", equalTo(post.dateCreated.toString()))
                .body("author.username", equalTo("user_0"))
                .body("anonymous", equalTo(false))
                .body("chapters.size()", equalTo(post.getChapters().size()))
                .body("commentCount", equalTo((int) Comment.count("post", post)))
                .body("voteCount", equalTo((int) Vote.count("post", post)));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void getOwnAnonymousPost() {
        given().get(anonymousPost.id.toString())
                .then()
                .statusCode(200)
                .body("id", equalTo(anonymousPost.id.toString()))
                .body("dateCreated", equalTo(anonymousPost.dateCreated.toString()))
                .body("author.username", equalTo("user_0"))
                .body("anonymous", equalTo(true))
                .body("chapters.size()", equalTo(anonymousPost.getChapters().size()))
                .body("commentCount", equalTo((int) Comment.count("post", anonymousPost)))
                .body("voteCount", equalTo((int) Vote.count("post", anonymousPost)));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void getOwnDraft() {
        given().get(draft.id.toString())
                .then()
                .statusCode(200)
                .body("id", equalTo(draft.id.toString()))
                .body("dateCreated", equalTo(draft.dateCreated.toString()))
                .body("author.username", equalTo("user_0"))
                .body("anonymous", equalTo(draft.anonymous))
                .body("chapters.size()", equalTo(draft.getChapters().size()));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void getOtherPost() {
        given().get(post.id.toString())
                .then()
                .statusCode(200)
                .body("id", equalTo(post.id.toString()))
                .body("dateCreated", equalTo(post.dateCreated.toString()))
                .body("author.username", equalTo("user_0"))
                .body("anonymous", equalTo(false))
                .body("chapters.size()", equalTo(post.getChapters().size()))
                .body("commentCount", equalTo((int) Comment.count("post", post)))
                .body("voteCount", equalTo((int) Vote.count("post", post)));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void getOtherAnonymousPost() {
        given().get(anonymousPost.id.toString())
                .then()
                .statusCode(200)
                .body("id", equalTo(anonymousPost.id.toString()))
                .body("dateCreated", equalTo(anonymousPost.dateCreated.toString()))
                .body("author", nullValue())
                .body("anonymous", equalTo(true))
                .body("chapters.size()", equalTo(anonymousPost.getChapters().size()))
                .body("commentCount", equalTo((int) Comment.count("post", anonymousPost)))
                .body("voteCount", equalTo((int) Vote.count("post", anonymousPost)));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void getOtherDraft() {
        given().get(draft.id.toString()).then().statusCode(404);
    }

    @Test
    @TestSecurity(user = "user_1")
    public void getOtherPostWhenBlocked() {
        QuarkusTransaction.requiringNew().run(() -> post.author.block(User.findByUsername("user_1")));
        given().get(post.id.toString()).then().statusCode(403);
    }

    @Test
    @TestSecurity(user = "user_1")
    public void getOtherAnonymousPostWhenBlocked() {
        QuarkusTransaction.requiringNew().run(() -> post.author.block(User.findByUsername("user_1")));
        given().get(anonymousPost.id.toString())
                .then()
                .statusCode(200)
                .body("id", equalTo(anonymousPost.id.toString()))
                .body("dateCreated", equalTo(anonymousPost.dateCreated.toString()))
                .body("author", nullValue())
                .body("anonymous", equalTo(true))
                .body("chapters.size()", equalTo(anonymousPost.getChapters().size()))
                .body("commentCount", equalTo((int) Comment.count("post", anonymousPost)))
                .body("voteCount", equalTo((int) Vote.count("post", anonymousPost)));
    }

    @Test
    public void getPostWhileUnauthenticated() {
        given().get(post.id.toString())
                .then()
                .statusCode(200)
                .body("id", equalTo(post.id.toString()))
                .body("dateCreated", equalTo(post.dateCreated.toString()))
                .body("author.username", equalTo("user_0"))
                .body("anonymous", equalTo(false))
                .body("chapters.size()", equalTo(post.getChapters().size()))
                .body("commentCount", equalTo((int) Comment.count("post", post)))
                .body("voteCount", equalTo((int) Vote.count("post", post)));
    }

    @Test
    public void getAnonymousPostWhileUnauthenticated() {
        given().get(anonymousPost.id.toString())
                .then()
                .statusCode(200)
                .body("id", equalTo(anonymousPost.id.toString()))
                .body("dateCreated", equalTo(anonymousPost.dateCreated.toString()))
                .body("author", nullValue())
                .body("anonymous", equalTo(true))
                .body("chapters.size()", equalTo(anonymousPost.getChapters().size()))
                .body("commentCount", equalTo((int) Comment.count("post", anonymousPost)))
                .body("voteCount", equalTo((int) Vote.count("post", anonymousPost)));
    }

    @Test
    public void getDraftWhileUnauthenticated() {
        given().get(draft.id.toString()).then().statusCode(404);
    }

    @Test
    public void getDeletedPost() {
        QuarkusTransaction.requiringNew()
                .run(() -> Post.<Post>findById(this.post.id).softDelete());
        given().get(post.id.toString()).then().statusCode(410);
    }

    @ParameterizedTest
    @ValueSource(strings = {"fake", "00000000-0000-0000-0000-000000000000"})
    public void getNonExistentPost(final String id) {
        given().get(id).then().statusCode(404);
    }
}
