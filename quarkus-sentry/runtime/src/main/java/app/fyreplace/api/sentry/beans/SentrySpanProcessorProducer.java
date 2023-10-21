package app.fyreplace.api.sentry.beans;

import io.sentry.opentelemetry.SentrySpanProcessor;
import jakarta.enterprise.context.ApplicationScoped;

public final class SentrySpanProcessorProducer {
    @SuppressWarnings("unused")
    @ApplicationScoped
    public SentrySpanProcessor produceSentrySpanProcessor() {
        return new SentrySpanProcessor();
    }
}
