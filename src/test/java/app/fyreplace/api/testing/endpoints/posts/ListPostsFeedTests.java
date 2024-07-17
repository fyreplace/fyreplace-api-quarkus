package app.fyreplace.api.testing.endpoints.posts;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.range;
import static org.hamcrest.CoreMatchers.equalTo;

import app.fyreplace.api.data.Block;
import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.User;
import app.fyreplace.api.data.Vote;
import app.fyreplace.api.data.dev.DataSeeder;
import app.fyreplace.api.endpoints.PostsEndpoint;
import app.fyreplace.api.testing.PostTestsBase;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(PostsEndpoint.class)
public final class ListPostsFeedTests extends PostTestsBase {
    @Inject
    DataSeeder dataSeeder;

    @Test
    @TestSecurity(user = "user_0")
    public void listPostsFeedWithOtherPosts() {
        final var user = requireNonNull(User.findByUsername("user_1"));
        range(0, 5).forEach(i -> dataSeeder.createPost(user, "Post " + i, true, false));
        final var response = given().get("feed").then().statusCode(200).body("size()", equalTo(3));
        range(0, 3).forEach(i -> response.body("[" + i + "].author.id", equalTo(user.id.toString())));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void listPostsFeedWithOtherDrafts() {
        final var user = requireNonNull(User.findByUsername("user_1"));
        range(0, 5).forEach(i -> dataSeeder.createPost(user, "Post " + i, false, false));
        given().get("feed").then().statusCode(200).body("size()", equalTo(0));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void listPostsFeedWithOwnPosts() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        range(0, 5).forEach(i -> dataSeeder.createPost(user, "Post " + i, true, false));
        given().get("feed").then().statusCode(200).body("size()", equalTo(0));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void listPostsFeedWithOwnDrafts() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        range(0, 5).forEach(i -> dataSeeder.createPost(user, "Post " + i, false, false));
        given().get("feed").then().statusCode(200).body("size()", equalTo(0));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void listPostsFeedWithAlreadyVotedPosts() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        final var otherUser = requireNonNull(User.findByUsername("user_1"));
        QuarkusTransaction.requiringNew().run(() -> range(0, 5).forEach(i -> {
            final var post = dataSeeder.createPost(otherUser, "Post " + i, true, false);
            final var vote = new Vote();
            vote.user = user;
            vote.post = post;
            vote.persist();
        }));

        given().get("feed").then().statusCode(200).body("size()", equalTo(0));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void listPostsFeedWithPostsFromBlockedUser() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        final var otherUser = requireNonNull(User.findByUsername("user_1"));
        QuarkusTransaction.requiringNew().run(() -> user.block(otherUser));
        range(0, 5).forEach(i -> dataSeeder.createPost(otherUser, "Post " + i, true, false));
        given().get("feed").then().statusCode(200).body("size()", equalTo(0));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void listPostsFeedWithPostsFromBlockingUser() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        final var otherUser = requireNonNull(User.findByUsername("user_1"));
        QuarkusTransaction.requiringNew().run(() -> user.block(otherUser));
        range(0, 5).forEach(i -> dataSeeder.createPost(otherUser, "Post " + i, true, false));
        given().get("feed").then().statusCode(200).body("size()", equalTo(0));
    }

    @Test
    public void listPostsFeedWhileUnauthenticated() {
        final var user = requireNonNull(User.findByUsername("user_1"));
        range(0, 5).forEach(i -> dataSeeder.createPost(user, "Post " + i, true, false));
        final var response = given().get("feed").then().statusCode(200).body("size()", equalTo(3));
        range(0, 3).forEach(i -> response.body("[" + i + "].author.id", equalTo(user.id.toString())));
    }

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        Post.deleteAll();
        Block.deleteAll();
    }
}
