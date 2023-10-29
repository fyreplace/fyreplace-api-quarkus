package app.fyreplace.api.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;

@MappedSuperclass
@FilterDef(name = "existing")
@Filter(name = "existing", condition = "deleted = false")
public abstract class SoftDeletableEntityBase extends TimestampedEntityBase {
    @Column(nullable = false)
    @JsonIgnore
    public boolean deleted;

    @Column(nullable = false)
    @JsonIgnore
    public boolean scrubbed;

    public void softDelete() {
        deleted = true;
        persist();
    }

    public void scrub() {
        deleted = true;
        scrubbed = true;
        persist();
    }
}
