package app.fyreplace.api.sentry.processors;

import app.fyreplace.api.sentry.config.SentryConfig;
import app.fyreplace.api.sentry.recorders.SentryRecorder;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LogHandlerBuildItem;
import io.quarkus.deployment.builditem.SystemPropertyBuildItem;
import io.sentry.opentelemetry.SentryContextStorageProvider;

@SuppressWarnings("unused")
public final class SentryProcessor {
    private static final String FEATURE = "sentry";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    SystemPropertyBuildItem setOtelAutoConfigure() {
        return new SystemPropertyBuildItem("otel.java.global-autoconfigure.enabled", "true");
    }

    @BuildStep
    SystemPropertyBuildItem setContextStorageProvider() {
        return new SystemPropertyBuildItem(
                "io.opentelemetry.context.contextStorageProvider",
                SentryContextStorageProvider.class.getCanonicalName());
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    LogHandlerBuildItem addSentryHandler(final SentryConfig config, final SentryRecorder recorder) {
        return new LogHandlerBuildItem(recorder.create(config));
    }
}
