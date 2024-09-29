package app.fyreplace.api.sentry.config;

import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;
import java.util.Optional;

@ConfigMapping(prefix = "quarkus.sentry")
@ConfigRoot(phase = ConfigPhase.RUN_TIME)
public interface SentryConfig {
    /**
     * Sentry Data Source Name.
     */
    Optional<String> dsn();

    /**
     * Environment the events are tagged with.
     */
    Optional<String> environment();

    /**
     * Percentage of performance events sent to Sentry.
     */
    @WithDefault("0.0")
    Optional<Double> tracesSampleRate();
}
