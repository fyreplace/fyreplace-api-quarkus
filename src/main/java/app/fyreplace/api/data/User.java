package app.fyreplace.api.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.SecurityContext;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "users")
public class User extends TimestampedEntityBase {
    public static final Set<String> forbiddenUsernames = new HashSet<String>(Arrays.asList(
            "admin",
            "admins",
            "administrator",
            "administrators",
            "anonymous",
            "author",
            "authors",
            "fyreplace",
            "fyreplaces",
            "management",
            "managements",
            "manager",
            "managers",
            "mod",
            "mods",
            "moderator",
            "moderators",
            "nil",
            "none",
            "nul",
            "null",
            "root",
            "roots",
            "superuser",
            "superusers",
            "sysadmin",
            "system",
            "systems",
            "systemadmin",
            "systemadmins",
            "systemadministrator",
            "systemadministrators",
            "systemsadmin",
            "systemsadmins",
            "systemsadministrator",
            "systemsadministrators",
            "user",
            "users",
            "void",
            "voids"));

    @Column(length = 100, unique = true, nullable = false)
    public String username;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JsonIgnore
    public Email mainEmail;

    @Column(nullable = false)
    @JsonIgnore
    public boolean isActive = false;

    @Column(nullable = false)
    public Rank rank = Rank.CITIZEN;

    @OneToOne(cascade = CascadeType.PERSIST)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JsonSerialize(using = StoredFile.Serializer.class)
    @Schema(implementation = String.class)
    public StoredFile avatar;

    @Column(length = 3000, nullable = false)
    public String bio = "";

    @Column(nullable = false)
    public boolean isBanned = false;

    @Column(nullable = false)
    @JsonIgnore
    public BanCount banCount = BanCount.NEVER;

    @JsonIgnore
    public Instant dateBanEnd;

    @JsonIgnore
    public Set<String> getGroups() {
        return Arrays.stream(Rank.values())
                .filter(group -> group.ordinal() <= rank.ordinal())
                .map(Rank::name)
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    public Profile getProfile() {
        return new Profile(id, username, avatar != null ? avatar.toString() : null);
    }

    @PostRemove
    final void postRemove() {
        if (avatar != null) {
            avatar.delete();
        }
    }

    public static User findByUsername(final String username) {
        return findByUsername(username, null);
    }

    public static User findByUsername(final String username, @Nullable final LockModeType lock) {
        return User.<User>find("username", username).withLock(lock).firstResult();
    }

    public static User getFromSecurityContext(final SecurityContext context) {
        return getFromSecurityContext(context, null);
    }

    public static User getFromSecurityContext(final SecurityContext context, final LockModeType lock) {
        final var user = findByUsername(context.getUserPrincipal().getName(), lock);

        if (user == null) {
            throw new NotAuthorizedException("Bearer");
        }

        return user;
    }

    public enum Rank {
        CITIZEN,
        MODERATOR,
        ADMINISTRATOR;
    }

    public enum BanCount {
        NEVER,
        ONCE,
        ONE_TOO_MANY;
    }

    public static final record Profile(@NotNull UUID id, @NotBlank String username, String avatar) {}
}
