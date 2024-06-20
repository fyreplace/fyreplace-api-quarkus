package app.fyreplace.api.testing;

import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.User;
import io.quarkus.narayana.jta.QuarkusTransaction;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;

public class SubscriptionTestsBase extends PostTestsBase {
    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        final var user = User.findByUsername("user_0");
        final var otherUser = User.findByUsername("user_1");

        try (final var stream = Post.<Post>stream("author = ?1 and published = true", user)) {
            stream.forEach(post -> dataSeeder.createComment(otherUser, post, "Comment", false));
        }

        dataSeeder.createComment(otherUser, post, "Comment", false);
    }
}
