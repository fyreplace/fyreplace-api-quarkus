package app.fyreplace.api.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.util.UUID;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "reports",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"source_id", "targetModel", "targetId"})})
public class Report extends TimestampedEntityBase {
    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    public User source;

    @Column(nullable = false)
    @JsonIgnore
    public Class<? extends Reportable> targetModel;

    @Column(nullable = false)
    public UUID targetId;

    @SuppressWarnings("unused")
    @JsonProperty("source")
    public User.Profile getSourceProfile() {
        return source.getProfile();
    }

    @SuppressWarnings("unused")
    @JsonProperty("targetModel")
    public String getTargetModelSimpleName() {
        return targetModel.getSimpleName();
    }
}
