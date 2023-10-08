package app.fyreplace.api.testing;

import static java.util.stream.IntStream.range;

import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;

public abstract class PostTestsBase extends UserTestsBase {
    public Post post;

    public Post draft;

    public Post anonymousPost;

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        final var user = User.findByUsername("user_0");
        range(0, getPostCount()).forEach(i -> dataSeeder.createPost(user, "Post " + i, true, false));
        range(0, getDraftCount()).forEach(i -> dataSeeder.createPost(user, "Draft " + i, false, false));
        post = Post.find("author.username = 'user_0' and published = true", Post.sorting())
                .firstResult();
        draft = Post.find("author.username = 'user_0' and published = false", Post.sorting())
                .firstResult();
        anonymousPost = dataSeeder.createPost(post.author, "Anonymous Post", true, true);
    }

    public int getPostCount() {
        return 3;
    }

    public int getDraftCount() {
        return 3;
    }
}
