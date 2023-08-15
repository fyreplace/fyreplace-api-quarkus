package app.fyreplace.api.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "emails")
public class Email extends EntityBase {
    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    public User user;

    @Column(length = 254, unique = true, nullable = false)
    public String email;

    @Column(nullable = false)
    public boolean verified = false;

    @JsonProperty("isMain")
    public boolean isMain() {
        return id.equals(user.mainEmail.id);
    }
}
