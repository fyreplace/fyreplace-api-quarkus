package app.fyreplace.api.data;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;

@MappedSuperclass
public abstract class EntityBase extends PanacheEntityBase {
    @Id
    @GeneratedValue
    public UUID id;

    public void refresh() {
        getEntityManager().refresh(this);
    }
}
