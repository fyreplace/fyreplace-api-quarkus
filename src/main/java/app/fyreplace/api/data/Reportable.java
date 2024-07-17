package app.fyreplace.api.data;

import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public interface Reportable {
    @Schema(required = true)
    UUID getId();

    default void reportBy(final User user) {
        if (isReportedBy(user)) {
            return;
        }

        final Report report = new Report();
        report.source = user;
        report.targetModel = getClass();
        report.targetId = getId();
        report.persist();
    }

    default void absolveBy(final User user) {
        Report.delete("source = ?1 and targetModel = ?2 and targetId = ?3", user, getClass(), getId());
    }

    default boolean isReportedBy(final User user) {
        return Report.count("source = ?1 and targetModel = ?2 and targetId = ?3", user, getClass(), getId()) > 0;
    }
}
