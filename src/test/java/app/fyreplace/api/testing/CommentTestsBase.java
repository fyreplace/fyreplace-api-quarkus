package app.fyreplace.api.testing;

import static java.util.stream.IntStream.range;

import app.fyreplace.api.data.User;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;

public class CommentTestsBase extends PostTestsBase {
    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        final var user = User.findByUsername("user_0");
        range(0, 10).forEach(i -> dataSeeder.createComment(user, post, "Comment " + i, false));
    }
}
