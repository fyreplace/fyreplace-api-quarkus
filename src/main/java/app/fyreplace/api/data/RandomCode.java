package app.fyreplace.api.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Duration;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "random_codes")
public class RandomCode extends TimestampedEntityBase {
    public static Duration lifetime = Duration.ofDays(1);

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Email email;

    @Column(nullable = false)
    public String code;

    @Override
    public String toString() {
        return code;
    }

    public void validateEmail() {
        email.verified = true;
        email.persist();
        delete();
    }
}
