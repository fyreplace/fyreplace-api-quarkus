package app.fyreplace.api.data;

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
}
