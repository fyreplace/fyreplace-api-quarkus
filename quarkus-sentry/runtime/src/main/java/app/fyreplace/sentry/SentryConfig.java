package app.fyreplace.sentry;

import io.quarkus.runtime.annotations.ConfigItem;
import io.quarkus.runtime.annotations.ConfigPhase;
import io.quarkus.runtime.annotations.ConfigRoot;
import java.util.Optional;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@ConfigRoot(phase = ConfigPhase.RUN_TIME, name = "sentry")
public final class SentryConfig {
    /**
     * Sentry Data Source Name.
     */
    @ConfigItem
    public Optional<String> dsn = Optional.empty();

    /**
     * Environment the events are tagged with.
     */
    @ConfigItem
    public Optional<String> environment = Optional.empty();

    /**
     * Which code release the events will belong to.
     */
    @ConfigItem
    public Optional<String> release = Optional.empty();

    /**
     * Percentage of performance events sent to Sentry.
     */
    @ConfigItem(defaultValue = "0.0")
    public Optional<Double> tracesSampleRate = Optional.empty();
}
