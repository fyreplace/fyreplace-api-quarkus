package app.fyreplace.api.testing.endpoints.posts;

import static io.restassured.RestAssured.given;
import static java.util.stream.IntStream.range;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.in;

import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.User;
import app.fyreplace.api.data.dev.DataSeeder;
import app.fyreplace.api.endpoints.PostsEndpoint;
import app.fyreplace.api.endpoints.PostsEndpoint.PostListingType;
import app.fyreplace.api.testing.TransactionalTestsBase;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(PostsEndpoint.class)
public final class ListTests extends TransactionalTestsBase {
    @ConfigProperty(name = "app.paging.size")
    int pagingSize;

    @Inject
    DataSeeder dataSeeder;

    private final List<String> subscribedToPostIds = new ArrayList<>();

    @Test
    @TestSecurity(user = "user_0")
    public void listSubscribedTo() {
        QuarkusTransaction.requiringNew().run(this::makeSubscribedToPosts);
        final var response = given().queryParam("page", 0)
                .queryParam("ascending", false)
                .queryParam("type", PostListingType.SUBSCRIBED_TO)
                .get()
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(pagingSize));

        range(0, pagingSize).forEach(i -> response.body("[" + i + "].id", in(subscribedToPostIds)));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void listPublished() {
        final var user = User.findByUsername("user_0");

        try (final var stream = Post.<Post>stream("author = ?1 and published = true", user)) {
            final var publishedPostIds = stream.map(post -> post.id.toString()).toList();
            final var response = given().queryParam("page", 0)
                    .queryParam("ascending", false)
                    .queryParam("type", PostListingType.PUBLISHED)
                    .get()
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("size()", equalTo(pagingSize));

            range(0, pagingSize).forEach(i -> response.body("[" + i + "].id", in(publishedPostIds)));
        }
    }

    @Test
    @TestSecurity(user = "user_0")
    public void listDrafts() {
        final var user = User.findByUsername("user_0");

        try (final var stream = Post.<Post>stream("author = ?1 and published = false", user)) {
            final var draftIds = stream.map(post -> post.id.toString()).toList();
            final var response = given().queryParam("page", 0)
                    .queryParam("ascending", false)
                    .queryParam("type", PostListingType.DRAFTS)
                    .get()
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("size()", equalTo(pagingSize));

            range(0, pagingSize).forEach(i -> response.body("[" + i + "].id", in(draftIds)));
        }
    }

    @Transactional
    public void makeSubscribedToPosts() {
        final var user0 = User.findByUsername("user_0");
        final var user1 = User.findByUsername("user_1");
        final var user2 = User.findByUsername("user_2");

        range(0, 20).forEach(i -> dataSeeder.createPost(user1, "Post " + i, true, false));
        range(0, 20).forEach(i -> dataSeeder.createPost(user2, "Post " + i, true, false));

        subscribedToPostIds.clear();

        try (final var stream = Post.<Post>stream("author", user1)) {
            stream.forEach(post -> {
                user0.subscribeTo(post);
                subscribedToPostIds.add(post.id.toString());
            });
        }

        try (final var stream = Post.<Post>stream("author", user0)) {
            stream.forEach(post -> subscribedToPostIds.add(post.id.toString()));
        }
    }
}
