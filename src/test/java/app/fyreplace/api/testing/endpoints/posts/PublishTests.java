package app.fyreplace.api.testing.endpoints.posts;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.Chapter;
import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.PostPublication;
import app.fyreplace.api.endpoints.PostsEndpoint;
import app.fyreplace.api.testing.PostTestsBase;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestHTTPEndpoint(PostsEndpoint.class)
public final class PublishTests extends PostTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void publishOwnPost() {
        given().contentType(ContentType.JSON)
                .body(new PostPublication(false))
                .post(post.id + "/publish")
                .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = "user_0")
    public void publishOwnDraft() {
        given().contentType(ContentType.JSON)
                .body(new PostPublication(true))
                .post(draft.id + "/publish")
                .then()
                .statusCode(200);
        assertEquals(0, Post.count("id = ?1 and published = false and anonymous = false", draft.id));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void publishAnonymouslyOwnDraft() {
        given().contentType(ContentType.JSON)
                .body(new PostPublication(false))
                .post(draft.id + "/publish")
                .then()
                .statusCode(200);
        assertEquals(0, Post.count("id = ?1 and published = false and anonymous = true", draft.id));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void publishOwnDraftWithoutChapters() {
        QuarkusTransaction.requiringNew().run(() -> Chapter.delete("post", draft));
        given().contentType(ContentType.JSON)
                .body(new PostPublication(false))
                .post(draft.id + "/publish")
                .then()
                .statusCode(403);
        assertEquals(1, Post.count("id = ?1 and published = false", draft.id));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void publishOtherPost() {
        given().contentType(ContentType.JSON)
                .body(new PostPublication(false))
                .post(post.id + "/publish")
                .then()
                .statusCode(403);
    }

    @Test
    @TestSecurity(user = "user_1")
    public void publishOtherDraft() {
        given().contentType(ContentType.JSON)
                .body(new PostPublication(false))
                .post(draft.id + "/publish")
                .then()
                .statusCode(404);
        assertEquals(1, Post.count("id = ?1 and published = false", draft.id));
    }

    @Test
    public void publishPostUnauthenticated() {
        given().contentType(ContentType.JSON)
                .body(new PostPublication(false))
                .post(post.id + "/publish")
                .then()
                .statusCode(401);
    }

    @Test
    public void publishDraftUnauthenticated() {
        given().contentType(ContentType.JSON)
                .body(new PostPublication(false))
                .post(draft.id + "/publish")
                .then()
                .statusCode(401);
        assertEquals(1, Post.count("id = ?1 and published = false", draft.id));
    }

    @ParameterizedTest
    @ValueSource(strings = {"fake", "00000000-0000-0000-0000-000000000000"})
    @TestSecurity(user = "user_0")
    public void publishNonExistent(final String id) {
        given().contentType(ContentType.JSON)
                .body(new PostPublication(false))
                .post(id + "/publish")
                .then()
                .statusCode(404);
    }
}
