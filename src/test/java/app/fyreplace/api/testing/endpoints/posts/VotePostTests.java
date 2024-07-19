package app.fyreplace.api.testing.endpoints.posts;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.User;
import app.fyreplace.api.data.Vote;
import app.fyreplace.api.data.VoteCreation;
import app.fyreplace.api.endpoints.PostsEndpoint;
import app.fyreplace.api.testing.PostTestsBase;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(PostsEndpoint.class)
public final class VotePostTests extends PostTestsBase {
    @Test
    @TestSecurity(user = "user_1")
    @Transactional
    public void votePostWithSpread() {
        final var voteCount = Vote.count();
        final var postLife = post.life;
        given().contentType(ContentType.JSON)
                .body(new VoteCreation(true))
                .post(post.id + "/vote")
                .then()
                .statusCode(200);
        assertEquals(voteCount + 1, Vote.count());
        final var vote =
                Vote.<Vote>find("post = ?1 and user.username = 'user_1'", post).firstResult();
        assertTrue(vote.spread);
        assertEquals(postLife + 1, vote.post.life);
    }

    @Test
    @TestSecurity(user = "user_1")
    @Transactional
    public void votePostWithoutSpread() {
        final var voteCount = Vote.count();
        final var postLife = post.life;
        given().contentType(ContentType.JSON)
                .body(new VoteCreation(false))
                .post(post.id + "/vote")
                .then()
                .statusCode(200);
        assertEquals(voteCount + 1, Vote.count());
        final var vote =
                Vote.<Vote>find("post = ?1 and user.username = 'user_1'", post).firstResult();
        assertFalse(vote.spread);
        assertEquals(postLife - 1, vote.post.life);
    }

    @Test
    @TestSecurity(user = "user_1")
    public void voteOldPost() {
        QuarkusTransaction.requiringNew()
                .run(() -> Post.update(
                        "dateCreated = ?1 where id = ?2",
                        Instant.now().minus(Post.shelfLife.plus(Duration.ofDays(1))),
                        post.id));
        final var voteCount = Vote.count();
        final var postLife = post.life;
        given().contentType(ContentType.JSON)
                .body(new VoteCreation(false))
                .post(post.id + "/vote")
                .then()
                .statusCode(403);
        assertEquals(voteCount, Vote.count());
        assertEquals(1, Post.count("id = ?1 and life = ?2", post.id, postLife));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void voteOldDraft() {
        QuarkusTransaction.requiringNew()
                .run(() -> Post.update(
                        "dateCreated = ?1 where id = ?2",
                        Instant.now().minus(Post.shelfLife.plus(Duration.ofDays(1))),
                        draft.id));
        final var voteCount = Vote.count();
        final var postLife = draft.life;
        given().contentType(ContentType.JSON)
                .body(new VoteCreation(false))
                .post(draft.id + "/vote")
                .then()
                .statusCode(404);
        assertEquals(voteCount, Vote.count());
        assertEquals(1, Post.count("id = ?1 and life = ?2", draft.id, postLife));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void voteOwnPost() {
        final var voteCount = Vote.count();
        final var postLife = post.life;
        given().contentType(ContentType.JSON)
                .body(new VoteCreation(false))
                .post(post.id + "/vote")
                .then()
                .statusCode(403);
        assertEquals(voteCount, Vote.count());
        assertEquals(1, Post.count("id = ?1 and life = ?2", post.id, postLife));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void voteOwnDraft() {
        final var voteCount = Vote.count();
        final var postLife = draft.life;
        given().contentType(ContentType.JSON)
                .body(new VoteCreation(false))
                .post(draft.id + "/vote")
                .then()
                .statusCode(403);
        assertEquals(voteCount, Vote.count());
        assertEquals(1, Post.count("id = ?1 and life = ?2", draft.id, postLife));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void voteOtherPostWhenBlocked() {
        final var user = User.findByUsername("user_1");
        QuarkusTransaction.requiringNew().run(() -> post.author.block(user));
        final var voteCount = Vote.count();
        final var postLife = post.life;
        given().contentType(ContentType.JSON)
                .body(new VoteCreation(false))
                .post(post.id + "/vote")
                .then()
                .statusCode(403);
        assertEquals(voteCount, Vote.count());
        assertEquals(1, Post.count("id = ?1 and life = ?2", post.id, postLife));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void voteOtherAnonymousPostWhenBlocked() {
        final var user = User.findByUsername("user_1");
        QuarkusTransaction.requiringNew().run(() -> anonymousPost.author.block(user));
        final var voteCount = Vote.count();
        given().contentType(ContentType.JSON)
                .body(new VoteCreation(true))
                .post(anonymousPost.id + "/vote")
                .then()
                .statusCode(200);
        assertEquals(voteCount + 1, Vote.count());
    }

    @Test
    @TestSecurity(user = "user_1")
    public void voteNonExistentPost() {
        final var voteCount = Vote.count();
        given().contentType(ContentType.JSON)
                .body(new VoteCreation(false))
                .post(fakeId + "/vote")
                .then()
                .statusCode(404);
        assertEquals(voteCount, Vote.count());
    }

    @Test
    @TestSecurity(user = "user_1")
    public void votePostWithEmptyInput() {
        final var voteCount = Vote.count();
        final var postLife = post.life;
        given().contentType(ContentType.JSON).post(post.id + "/vote").then().statusCode(400);
        assertEquals(voteCount, Vote.count());
        assertEquals(1, Post.count("id = ?1 and life = ?2", post.id, postLife));
    }

    @Test
    public void votePostWhileUnauthenticated() {
        final var voteCount = Vote.count();
        given().contentType(ContentType.JSON)
                .body(new VoteCreation(false))
                .post(fakeId + "/vote")
                .then()
                .statusCode(401);
        assertEquals(voteCount, Vote.count());
    }
}
