package app.fyreplace.api.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SourceType;

@Entity
@Table(name = "random_codes")
public class RandomCode extends EntityBase {
    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Email email;

    @Column(nullable = false)
    public String code;

    @Column(nullable = false)
    @CreationTimestamp(source = SourceType.DB)
    public Instant dateCreated;

    @Override
    public String toString() {
        return code;
    }
}
