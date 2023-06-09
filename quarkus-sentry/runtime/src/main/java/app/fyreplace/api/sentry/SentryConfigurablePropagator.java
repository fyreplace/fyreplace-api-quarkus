package app.fyreplace.api.sentry;

import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurablePropagatorProvider;
import io.sentry.opentelemetry.SentryPropagator;
import jakarta.annotation.Nonnull;

public final class SentryConfigurablePropagator implements ConfigurablePropagatorProvider {
    @Override
    public TextMapPropagator getPropagator(@Nonnull final ConfigProperties config) {
        return new SentryPropagator();
    }

    @Override
    public String getName() {
        return "sentry";
    }
}
