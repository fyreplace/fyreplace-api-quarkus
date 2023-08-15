package app.fyreplace.api.testing.endpoints.posts;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.Post;
import app.fyreplace.api.endpoints.PostsEndpoint;
import app.fyreplace.api.testing.endpoints.PostTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestHTTPEndpoint(PostsEndpoint.class)
public final class DeleteTests extends PostTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void deleteOwnPost() {
        given().delete(post.id.toString()).then().statusCode(204);
        assertEquals(0, Post.count("id", post.id));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void deleteOwnDraft() {
        given().delete(draft.id.toString()).then().statusCode(204);
        assertEquals(0, Post.count("id", draft.id));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void deleteOtherPost() {
        given().delete(post.id.toString()).then().statusCode(403);
        assertEquals(1, Post.count("id", post.id));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void deleteOtherDraft() {
        given().delete(draft.id.toString()).then().statusCode(404);
        assertEquals(1, Post.count("id", draft.id));
    }

    @Test
    public void deletePostUnauthenticated() {
        given().delete(post.id.toString()).then().statusCode(401);
        assertEquals(1, Post.count("id", post.id));
    }

    @Test
    public void deleteDraftUnauthenticated() {
        given().delete(draft.id.toString()).then().statusCode(401);
        assertEquals(1, Post.count("id", draft.id));
    }

    @ParameterizedTest
    @ValueSource(strings = {"fake", "00000000-0000-0000-0000-000000000000"})
    @TestSecurity(user = "user_0")
    public void deleteNonExistent(final String id) {
        given().delete(id).then().statusCode(404);
    }
}
