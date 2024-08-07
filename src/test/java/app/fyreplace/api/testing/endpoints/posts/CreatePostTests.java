package app.fyreplace.api.testing.endpoints.posts;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.Post;
import app.fyreplace.api.endpoints.PostsEndpoint;
import app.fyreplace.api.testing.PostTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(PostsEndpoint.class)
public final class CreatePostTests extends PostTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void createPost() {
        final var postCount = Post.count("author.username = 'user_0'");
        given().post()
                .then()
                .statusCode(201)
                .body("id", isA(String.class))
                .body("dateCreated", notNullValue())
                .body("published", equalTo(false))
                .body("author.username", equalTo("user_0"))
                .body("anonymous", equalTo(false))
                .body("chapters.size()", equalTo(0));
        assertEquals(postCount + 1, Post.count("author.username = 'user_0'"));
    }

    @Test
    public void createPostWhileUnauthenticated() {
        final var postCount = Post.count("author.username = 'user_0'");
        given().post().then().statusCode(401);
        assertEquals(postCount, Post.count());
    }
}
