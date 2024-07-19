package app.fyreplace.api.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nullable;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

@MappedSuperclass
public abstract class UserDependentEntityBase extends SoftDeletableEntityBase {
    @Transient
    @JsonIgnore
    @Nullable
    public User currentUser;

    public void setCurrentUser(@Nullable final User user) {
        currentUser = user;
    }
}
