package app.fyreplace.sentry;

import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.annotations.Recorder;
import io.sentry.Instrumenter;
import io.sentry.Sentry;
import io.sentry.SentryOptions;
import io.sentry.jul.SentryHandler;
import io.sentry.opentelemetry.OpenTelemetryLinkErrorEventProcessor;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;

@Recorder
public class SentryRecorder {
    public RuntimeValue<Optional<Handler>> create(final SentryConfig config) {
        if (config.dsn.isEmpty()) {
            return new RuntimeValue<>(Optional.empty());
        }

        final var options = new AtomicReference<SentryOptions>();
        Sentry.init(it -> {
            it.setDsn(config.dsn.get());
            config.environment.ifPresent(it::setEnvironment);
            config.release.ifPresent(it::setRelease);
            config.tracesSampleRate.ifPresent(it::setTracesSampleRate);
            it.addInAppInclude("app.fyreplace");
            it.setInstrumenter(Instrumenter.OTEL);
            it.addEventProcessor(new OpenTelemetryLinkErrorEventProcessor());
            options.set(it);
        });

        final var handler = new SentryHandler(options.get());
        handler.setPrintfStyle(true);
        handler.setLevel(Level.WARNING);
        handler.setMinimumEventLevel(Level.WARNING);
        handler.setMinimumBreadcrumbLevel(Level.INFO);
        return new RuntimeValue<>(Optional.of(handler));
    }
}
