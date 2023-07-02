package app.fyreplace.api.data.dev;

import static java.util.stream.IntStream.range;

import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.RandomCode;
import app.fyreplace.api.data.StoredFile;
import app.fyreplace.api.data.User;
import io.quarkus.runtime.LaunchMode;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.runtime.configuration.ProfileManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class DataSeeder {
    @ConfigProperty(name = "app.use-example-data")
    private boolean useExampleData;

    public void onStartup(@Observes final StartupEvent event) {
        if (shouldUseExampleData()) {
            insertData();
        }
    }

    public void onShutdown(@Observes final ShutdownEvent event) {
        if (shouldUseExampleData()) {
            deleteData();
        }
    }

    @Transactional
    public void insertData() {
        range(0, 100).forEach(i -> createUser("user_" + i, true));
        range(0, 10).forEach(i -> createUser("user_inactive_" + i, false));
    }

    @Transactional
    public void deleteData() {
        Email.deleteAll();
        RandomCode.deleteAll();
        User.deleteAll();
        StoredFile.<StoredFile>streamAll().forEach(StoredFile::delete);
    }

    private boolean shouldUseExampleData() {
        return useExampleData && ProfileManager.getLaunchMode() == LaunchMode.DEVELOPMENT;
    }

    private void createUser(final String username, final boolean isActive) {
        final var user = new User();
        user.username = username;
        user.isActive = isActive;
        user.persist();

        final var email = new Email();
        email.user = user;
        email.email = username + "@example.org";
        email.isVerified = isActive;
        email.persist();

        user.mainEmail = email;
        user.persist();
    }
}
