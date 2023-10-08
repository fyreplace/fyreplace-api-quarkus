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
public final class VoteTests extends PostTestsBase {
    @Test
    @TestSecurity(user = "user_1")
    @Transactional
    public void voteWithSpread() {
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
        assertTrue(vote.isSpread);
        assertEquals(postLife + 1, vote.post.life);
    }

    @Test
    @TestSecurity(user = "user_1")
    @Transactional
    public void voteWithoutSpread() {
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
        assertFalse(vote.isSpread);
        assertEquals(postLife - 1, vote.post.life);
    }

    @Test
    @TestSecurity(user = "user_1")
    public void voteOnOldPost() {
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
    public void voteOnOldDraft() {
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
    public void voteOnOwnPost() {
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
    public void voteOnOwnDraft() {
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
    public void voteOnOtherPostWhenBlocked() {
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
    public void voteOnOtherAnonymousPostWhenBlocked() {
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
    public void voteOnNonExistentPost() {
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
    public void voteWithEmptyInput() {
        final var voteCount = Vote.count();
        final var postLife = post.life;
        given().contentType(ContentType.JSON).post(post.id + "/vote").then().statusCode(400);
        assertEquals(voteCount, Vote.count());
        assertEquals(1, Post.count("id = ?1 and life = ?2", post.id, postLife));
    }

    @Test
    public void voteUnauthenticated() {
        final var voteCount = Vote.count();
        given().contentType(ContentType.JSON)
                .body(new VoteCreation(false))
                .post(fakeId + "/vote")
                .then()
                .statusCode(401);
        assertEquals(voteCount, Vote.count());
    }
}
