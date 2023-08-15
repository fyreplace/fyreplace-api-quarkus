package app.fyreplace.api.tasks;

import app.fyreplace.api.data.RandomCode;
import app.fyreplace.api.data.User;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;

@ApplicationScoped
public final class CleanupTasks {
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void removeOldInactiveUsers() {
        User.delete("active = false and dateCreated < ?1", oneDayAgo());
    }

    @Scheduled(cron = "0 5 * * * ?")
    @Transactional
    public void removeOldRandomCodes() {
        RandomCode.delete("dateCreated < ?1", oneDayAgo());
    }

    private Instant oneDayAgo() {
        return Instant.now().minus(Duration.ofDays(1));
    }
}
