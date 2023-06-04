package app.fyreplace.sentry;

import io.sentry.opentelemetry.SentrySpanProcessor;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

@ApplicationScoped
public final class SentrySpanProcessorProducer {
    @Produces
    @ApplicationScoped
    public SentrySpanProcessor produceSentrySpanProcessor() {
        return new SentrySpanProcessor();
    }
}
