package app.fyreplace.api.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "push_notification_tokens")
public class PushNotificationToken extends EntityBase {
    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    public User user;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    public Service service;

    @Column(nullable = false)
    public String token;

    public enum Service {
        APPLE,
        FIREBASE,
        HUAWEI,
        SAMSUNG,
        WEB,
        WINDOWS
    }
}
