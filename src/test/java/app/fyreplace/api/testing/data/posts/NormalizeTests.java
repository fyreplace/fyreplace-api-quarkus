package app.fyreplace.api.testing.data.posts;

import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.Chapter;
import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.User;
import app.fyreplace.api.testing.PostTestsBase;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
public final class NormalizeTests extends PostTestsBase {
    private Post post;

    private Post emptyPost;

    @Test
    @Transactional
    public void normalize() {
        post.normalize();
        assertPostNormalized(post);
    }

    @Test
    @Transactional
    public void normalizeTwice() {
        post.normalize();
        post.normalize();
        assertPostNormalized(post);
    }

    @Test
    @Transactional
    public void normalizeEmpty() {
        emptyPost.normalize();
        assertEquals(0, emptyPost.getChapters().size());
        assertPostNormalized(emptyPost);
    }

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        final var user = User.findByUsername("user_0");
        final var chapterPositions = List.of("azaz", "azz", "zzazaz");

        post = new Post();
        post.author = user;
        post.persist();

        for (final var position : chapterPositions) {
            final var chapter = new Chapter();
            chapter.post = post;
            chapter.position = position;
            chapter.persist();
        }

        emptyPost = new Post();
        emptyPost.author = user;
        emptyPost.persist();
    }

    private void assertPostNormalized(final Post post) {
        final var chapters = post.getChapters();

        for (var i = 0; i < chapters.size(); i++) {
            assertEquals("z".repeat(i + 1), chapters.get(i).position);
        }
    }
}
