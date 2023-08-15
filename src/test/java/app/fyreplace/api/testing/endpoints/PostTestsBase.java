package app.fyreplace.api.testing.endpoints;

import app.fyreplace.api.data.Post;
import app.fyreplace.api.testing.ImageTests;
import io.quarkus.panache.common.Sort;
import org.junit.jupiter.api.BeforeEach;

public abstract class PostTestsBase extends ImageTests {
    public Post post;

    public Post draft;

    @BeforeEach
    @Override
    public void beforeEach() {
        super.beforeEach();
        post = Post.find("author.username = 'user_0' and datePublished is not null", Sort.by("datePublished", "id"))
                .firstResult();
        draft = Post.find("author.username = 'user_0' and datePublished is null", Sort.by("datePublished", "id"))
                .firstResult();
    }
}
