package app.fyreplace.api.data;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "reports",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"source_id", "targetModel", "targetId"})})
public class Report extends TimestampedEntityBase {
    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonSerialize(using = User.ProfileSerializer.class)
    public User source;

    @Column(nullable = false)
    @JsonSerialize(using = Reportable.Serializer.class)
    public Class<? extends Reportable> targetModel;

    @Column(nullable = false)
    @Schema(required = true)
    public UUID targetId;
}
