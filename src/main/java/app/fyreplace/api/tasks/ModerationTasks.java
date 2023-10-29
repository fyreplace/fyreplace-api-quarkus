package app.fyreplace.api.tasks;

import app.fyreplace.api.data.User;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public final class ModerationTasks {
    @Scheduled(cron = "0 15 * * * ?")
    @Transactional
    public void welcomeBackBannedUsers() {
        User.update(
                """
                banned = false, dateBanEnd = null
                where banned and dateBanEnd < current_timestamp
                """);
    }
}
