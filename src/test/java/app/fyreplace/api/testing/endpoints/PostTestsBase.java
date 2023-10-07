package app.fyreplace.api.testing.endpoints;

import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.dev.DataSeeder;
import app.fyreplace.api.testing.ImageTests;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;

public abstract class PostTestsBase extends ImageTests {
    @Inject
    DataSeeder dataSeeder;

    public Post post;

    public Post draft;

    public Post anonymousPost;

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        post = Post.find("author.username = 'user_0' and published = true", Post.sorting())
                .firstResult();
        draft = Post.find("author.username = 'user_0' and published = false", Post.sorting())
                .firstResult();
        anonymousPost = dataSeeder.createPost(post.author, "Anonymous Post", true, true);
    }
}
