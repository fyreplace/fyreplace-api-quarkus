package app.fyreplace.api.tasks;

import app.fyreplace.api.data.Comment;
import app.fyreplace.api.data.Post;
import app.fyreplace.api.data.RandomCode;
import app.fyreplace.api.data.User;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@ApplicationScoped
public final class CleanupTasks {

    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void scrubSoftDeletedEntities() {
        try (final var stream = User.<User>stream(scrubConditions())) {
            stream.forEach(User::scrub);
        }

        try (final var stream = Post.<Post>stream(scrubConditions("author"))) {
            stream.forEach(Post::scrub);
        }

        try (final var stream = Comment.<Comment>stream(scrubConditions("author", "post"))) {
            stream.forEach(Comment::scrub);
        }
    }

    @Scheduled(cron = "0 5 * * * ?")
    @Transactional
    public void removeOldInactiveUsers() {
        User.delete("active = false and dateCreated < ?1", Instant.now().minus(User.LIFETIME));
    }

    @Scheduled(cron = "0 10 * * * ?")
    @Transactional
    public void removeOldRandomCodes() {
        RandomCode.delete("dateCreated < ?1", Instant.now().minus(RandomCode.LIFETIME));
    }

    private String scrubConditions(final String... fields) {
        return "scrubbed = false and ("
                + Stream.concat(Stream.of(""), Arrays.stream(fields).map(field -> field + '.'))
                        .map(fieldDot -> fieldDot + "deleted = true")
                        .collect(Collectors.joining(" or "))
                + ')';
    }
}
