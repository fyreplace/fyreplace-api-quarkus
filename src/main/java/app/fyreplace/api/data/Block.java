package app.fyreplace.api.data;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "blocks", uniqueConstraints = @UniqueConstraint(columnNames = {"source_id", "target_id"}))
public class Block extends EntityBase {
    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public User source;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public User target;
}
