package app.fyreplace.api.testing.tasks.cleanup;

import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.fyreplace.api.data.Chapter;
import app.fyreplace.api.data.Comment;
import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.User;
import app.fyreplace.api.tasks.CleanupTasks;
import app.fyreplace.api.testing.CommentTestsBase;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;

@QuarkusTest
public final class ScrubSoftDeletedEntitiesTests extends CommentTestsBase {
    @Inject
    CleanupTasks cleanupTasks;

    @Test
    @Transactional
    public void removeSoftDeletedUser() {
        final var userCount = User.count();
        final var user = requireNonNull(User.findByUsername("user_0"));
        QuarkusTransaction.requiringNew().run(() -> User.<User>findById(user.id).softDelete());
        cleanupTasks.scrubSoftDeletedEntities();
        assertEquals(userCount, User.count());
        user.refresh();
        assertTrue(user.deleted);
        assertTrue(user.scrubbed);
        assertNull(user.username);
        assertNull(user.mainEmail);
        assertNull(user.avatar);
        assertEquals("", user.bio);
        assertEquals(0, Email.count("user", user));
        assertEquals(0, Post.count("author = ?1 and deleted = false", user));
        assertEquals(0, Comment.count("author = ?1 and deleted = false", user));
    }

    @Test
    @Transactional
    public void removeSoftDeletedPost() {
        final var postCount = Post.count();
        QuarkusTransaction.requiringNew().run(() -> Post.<Post>findById(post.id).softDelete());
        cleanupTasks.scrubSoftDeletedEntities();
        assertEquals(postCount, Post.count());
        final var post = Post.<Post>findById(this.post.id);
        assertTrue(post.deleted);
        assertTrue(post.scrubbed);
        assertEquals(0, Chapter.count("post", post));
        assertEquals(0, Comment.count("post = ?1 and deleted = false", post));
    }

    @Test
    @Transactional
    public void removeSoftDeletedComment() {
        final var commentCount = Comment.count();
        final var comment = Comment.<Comment>find("post", post).firstResult();
        QuarkusTransaction.requiringNew()
                .run(() -> Comment.<Comment>findById(comment.id).softDelete());
        cleanupTasks.scrubSoftDeletedEntities();
        assertEquals(commentCount, Comment.count());
        comment.refresh();
        assertTrue(comment.deleted);
        assertTrue(comment.scrubbed);
        assertEquals("", comment.text);
    }
}
