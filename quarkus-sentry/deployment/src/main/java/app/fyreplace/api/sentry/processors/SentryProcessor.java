package app.fyreplace.api.sentry.processors;

import app.fyreplace.api.sentry.beans.SentrySpanProcessorProducer;
import app.fyreplace.api.sentry.config.SentryConfig;
import app.fyreplace.api.sentry.recorders.SentryRecorder;
import io.quarkus.arc.deployment.AdditionalBeanBuildItem;
import io.quarkus.deployment.annotations.BuildStep;
import io.quarkus.deployment.annotations.ExecutionTime;
import io.quarkus.deployment.annotations.Record;
import io.quarkus.deployment.builditem.FeatureBuildItem;
import io.quarkus.deployment.builditem.LogHandlerBuildItem;

public final class SentryProcessor {
    private static final String FEATURE = "sentry";

    @BuildStep
    FeatureBuildItem feature() {
        return new FeatureBuildItem(FEATURE);
    }

    @BuildStep
    @Record(ExecutionTime.RUNTIME_INIT)
    LogHandlerBuildItem addSentryHandler(final SentryConfig config, final SentryRecorder recorder) {
        return new LogHandlerBuildItem(recorder.create(config));
    }

    @BuildStep
    AdditionalBeanBuildItem addSentrySpanProcessorProducer() {
        return AdditionalBeanBuildItem.builder()
                .addBeanClass(SentrySpanProcessorProducer.class)
                .build();
    }
}
