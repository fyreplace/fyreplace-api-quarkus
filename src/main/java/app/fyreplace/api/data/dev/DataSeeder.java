package app.fyreplace.api.data.dev;

import static java.util.stream.IntStream.range;

import app.fyreplace.api.data.Block;
import app.fyreplace.api.data.Chapter;
import app.fyreplace.api.data.Comment;
import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.Password;
import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.RandomCode;
import app.fyreplace.api.data.Report;
import app.fyreplace.api.data.StoredFile;
import app.fyreplace.api.data.Subscription;
import app.fyreplace.api.data.User;
import app.fyreplace.api.data.Vote;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@SuppressWarnings({"UnusedReturnValue", "unused"})
@ApplicationScoped
public final class DataSeeder {
    @ConfigProperty(name = "app.local-dev")
    boolean useExampleData;

    @ConfigProperty(name = "app.posts.starting-life")
    int postsStartingLife;

    public void onStartup(@Observes final StartupEvent event) {
        if (useExampleData) {
            insertData();
        }
    }

    public void onShutdown(@Observes final ShutdownEvent event) {
        if (useExampleData) {
            deleteData();
        }
    }

    @Transactional
    public void insertData() {
        range(0, 20).forEach(i -> createUser("user_" + i, true));
        range(0, 10).forEach(i -> createUser("user_inactive_" + i, false));
        final var user = User.findByUsername("user_0");
        range(0, 20).forEach(i -> createPost(user, "Post " + i, true, false));
        range(0, 20).forEach(i -> createPost(user, "Draft " + i, false, false));
        final var post = Post.<Post>find("author = ?1 and published = true", Post.sorting(), user)
                .firstResult();
        range(0, 10).forEach(i -> createComment(user, post, "Comment " + i, false));
    }

    @Transactional
    public void deleteData() {
        Email.deleteAll();
        Password.deleteAll();
        RandomCode.deleteAll();
        Block.deleteAll();
        Report.deleteAll();
        User.deleteAll();
        Subscription.deleteAll();
        Vote.deleteAll();
        Chapter.deleteAll();
        Post.deleteAll();
        Comment.deleteAll();

        try (final var stream = StoredFile.<StoredFile>streamAll()) {
            stream.forEach(StoredFile::delete);
        }
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public User createUser(final String username, final boolean active) {
        final var user = new User();
        user.username = username;
        user.active = active;
        user.persist();

        final var email = new Email();
        email.user = user;
        email.email = username + "@example.org";
        email.verified = active;
        email.persist();

        user.mainEmail = email;
        user.persist();
        return user;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Post createPost(final User author, final String text, final boolean published, final boolean anonymous) {
        final var post = new Post();
        post.author = author;
        post.persist();
        String before = null;

        for (var i = 0; i < 3; i++) {
            final var chapter = new Chapter();
            chapter.post = post;
            chapter.position = Chapter.positionBetween(before, null);
            chapter.text = text + ' ' + i;
            chapter.persist();
            before = chapter.position;
        }

        if (published) {
            post.publish(postsStartingLife, anonymous);
        }

        return post;
    }

    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public Comment createComment(final User author, final Post post, final String text, final boolean anonymous) {
        final var comment = new Comment();
        comment.author = author;
        comment.anonymous = anonymous;
        comment.post = post;
        comment.text = text;
        comment.persist();
        return comment;
    }
}
