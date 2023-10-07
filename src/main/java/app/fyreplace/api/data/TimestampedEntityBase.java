package app.fyreplace.api.data;

import io.quarkus.panache.common.Sort;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;

@MappedSuperclass
public abstract class TimestampedEntityBase extends EntityBase {
    @Column(nullable = false)
    @CreationTimestamp(source = SourceType.DB)
    public Instant dateCreated;

    public static Sort sorting() {
        return Sort.by("dateCreated", "id");
    }
}
