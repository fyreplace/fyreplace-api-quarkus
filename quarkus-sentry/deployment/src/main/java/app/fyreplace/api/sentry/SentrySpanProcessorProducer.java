package app.fyreplace.api.sentry;

import io.sentry.opentelemetry.SentrySpanProcessor;
import jakarta.enterprise.context.ApplicationScoped;

public final class SentrySpanProcessorProducer {
    @ApplicationScoped
    public SentrySpanProcessor produceSentrySpanProcessor() {
        return new SentrySpanProcessor();
    }
}
