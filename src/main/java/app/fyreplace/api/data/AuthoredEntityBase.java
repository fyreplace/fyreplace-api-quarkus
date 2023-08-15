package app.fyreplace.api.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@MappedSuperclass
public class AuthoredEntityBase extends TimestampedEntityBase {
    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    public User author;

    @Column(nullable = false)
    public boolean anonymous = false;

    @Transient
    @JsonIgnore
    @Nullable
    public User currentUser;

    @SuppressWarnings("unused")
    @JsonProperty("author")
    @Nullable
    public User.Profile getAuthorProfile() {
        if (anonymous && (currentUser == null || !currentUser.id.equals(author.id))) {
            return null;
        }

        return author.getProfile();
    }

    public void setCurrentUser(@Nullable final User user) {
        currentUser = user;
    }
}
