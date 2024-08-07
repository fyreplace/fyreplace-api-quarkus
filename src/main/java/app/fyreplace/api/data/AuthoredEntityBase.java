package app.fyreplace.api.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@MappedSuperclass
public abstract class AuthoredEntityBase extends UserDependentEntityBase {
    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    public User author;

    @Column(nullable = false)
    @Schema(required = true)
    public boolean anonymous = false;

    @SuppressWarnings("unused")
    @JsonProperty("author")
    @Nullable
    public User.Profile getAuthorProfile() {
        if (anonymous && (currentUser == null || !currentUser.id.equals(author.id))) {
            return null;
        }

        return author.getProfile();
    }
}
