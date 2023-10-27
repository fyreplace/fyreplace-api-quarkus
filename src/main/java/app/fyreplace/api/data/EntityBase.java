package app.fyreplace.api.data;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;
import lombok.Getter;
import org.hibernate.annotations.UuidGenerator;

@Getter
@MappedSuperclass
public abstract class EntityBase extends PanacheEntityBase {
    @Id
    @GeneratedValue
    @UuidGenerator(style = UuidGenerator.Style.RANDOM)
    public UUID id;

    public void refresh() {
        getEntityManager().refresh(this);
    }
}
