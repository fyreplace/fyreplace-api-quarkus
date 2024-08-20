package app.fyreplace.api.testing.endpoints.posts;

import static io.restassured.RestAssured.given;
import static java.util.stream.IntStream.range;
import static org.hamcrest.Matchers.equalTo;

import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.User;
import app.fyreplace.api.data.dev.DataSeeder;
import app.fyreplace.api.endpoints.PostsEndpoint;
import app.fyreplace.api.testing.PostTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(PostsEndpoint.class)
public final class CountPostsTests extends PostTestsBase {
    @Inject
    DataSeeder dataSeeder;

    private static final int PUBLISHED_POST_COUNT = 10;

    private static final int DRAFT_POST_COUNT = 6;

    @Test
    @TestSecurity(user = "user_0")
    public void countSubscribedPosts() {
        given().queryParam("type", PostsEndpoint.PostListingType.SUBSCRIBED_TO)
                .get("count")
                .then()
                .statusCode(200)
                .body(equalTo(String.valueOf(PUBLISHED_POST_COUNT)));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void countPublishedPosts() {
        given().queryParam("type", PostsEndpoint.PostListingType.PUBLISHED)
                .get("count")
                .then()
                .statusCode(200)
                .body(equalTo(String.valueOf(PUBLISHED_POST_COUNT)));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void countDrafts() {
        given().queryParam("type", PostsEndpoint.PostListingType.DRAFTS)
                .get("count")
                .then()
                .statusCode(200)
                .body(equalTo(String.valueOf(DRAFT_POST_COUNT)));
    }

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        Post.deleteAll();
        final var user = User.findByUsername("user_0");
        range(0, PUBLISHED_POST_COUNT).forEach(i -> dataSeeder.createPost(user, "Post " + i, true, false));
        range(0, DRAFT_POST_COUNT).forEach(i -> dataSeeder.createPost(user, "Post " + i, false, false));
        range(0, 10).forEach(i -> dataSeeder.createPost(User.findByUsername("user_1"), "Post " + i, false, false));
    }
}
