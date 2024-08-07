package app.fyreplace.api.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "emails")
public class Email extends EntityBase {
    public static final int EMAIL_MIN_LENGTH = 3;
    public static final int EMAIL_MAX_LENGTH = 254;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    public User user;

    @Column(length = EMAIL_MAX_LENGTH, unique = true, nullable = false)
    @Schema(required = true, minLength = EMAIL_MIN_LENGTH, maxLength = EMAIL_MAX_LENGTH)
    public String email;

    @Column(nullable = false)
    @Schema(required = true)
    public boolean verified = false;

    @JsonProperty("main")
    @Schema(required = true)
    public boolean isMain() {
        return id.equals(user.mainEmail.id);
    }
}
