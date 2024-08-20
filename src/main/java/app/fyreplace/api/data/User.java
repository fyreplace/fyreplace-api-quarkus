package app.fyreplace.api.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.sentry.Sentry;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.LockModeType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostRemove;
import jakarta.persistence.Table;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.SecurityContext;
import java.awt.Color;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.CRC32;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "users")
public class User extends UserDependentEntityBase implements Reportable {
    public static final Set<String> FORBIDDEN_USERNAMES = new HashSet<>(Arrays.asList(
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
            "username",
            "usernames",
            "void",
            "voids"));

    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int USERNAME_MAX_LENGTH = 50;
    public static final String USERNAME_PATTERN = "^[\\w.@+-]+$";
    public static final int BIO_MAX_LENGTH = 3000;
    public static Duration LIFETIME = Duration.ofDays(1);

    @Column(length = USERNAME_MAX_LENGTH, unique = true)
    @Schema(
            required = true,
            minLength = USERNAME_MIN_LENGTH,
            maxLength = USERNAME_MAX_LENGTH,
            pattern = USERNAME_PATTERN)
    public String username;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JsonIgnore
    public Email mainEmail;

    @Column(nullable = false)
    @JsonIgnore
    public boolean active = false;

    @Column(nullable = false)
    @Schema(required = true)
    public Rank rank = Rank.CITIZEN;

    @OneToOne(cascade = CascadeType.PERSIST)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JsonSerialize(using = StoredFile.Serializer.class)
    @Schema(required = true, implementation = String.class)
    public StoredFile avatar;

    @Column(length = BIO_MAX_LENGTH, nullable = false)
    @Schema(required = true)
    public String bio = "";

    @Column(nullable = false)
    @Schema(required = true)
    public boolean banned = false;

    @Column(nullable = false)
    @JsonIgnore
    public BanCount banCount = BanCount.NEVER;

    @JsonIgnore
    public Instant dateBanEnd;

    @Override
    public void scrub() {
        super.scrub();
        Email.delete("user", this);
        username = null;
        mainEmail = null;
        bio = "";

        if (avatar != null) {
            avatar.delete();
        }
    }

    @JsonIgnore
    public Set<String> getGroups() {
        return Arrays.stream(Rank.values())
                .filter(group -> group.ordinal() <= rank.ordinal())
                .map(Rank::name)
                .collect(Collectors.toSet());
    }

    @JsonIgnore
    public Profile getProfile() {
        return new Profile(id, username, avatar != null ? avatar.toString() : null, getTint());
    }

    @SuppressWarnings("unused")
    @Schema(required = true)
    public boolean getBlocked() {
        return Block.count("source = ?1 and target = ?2", currentUser, this) > 0;
    }

    @Schema(required = true, pattern = "^#[A-F0-9]{6}$")
    public String getTint() {
        final var crc = new CRC32();
        crc.update(username.getBytes());
        final var hue = (float) (crc.getValue() / Math.pow(2, 32));
        final var h = hue * 6;
        final var variance = (h - (float) Math.floor(h)) * 0.25f;
        final var brightness = (int) h % 2 == 0 ? 1f - variance : 0.75f + variance;
        final var color = Color.HSBtoRGB(hue, 0.5f, brightness);
        return "#%02X%02X%02X".formatted((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF);
    }

    @Override
    public void softDelete() {
        super.softDelete();
        Subscription.delete("user", this);
    }

    @SuppressWarnings("unused")
    @PostRemove
    final void postRemove() {
        if (avatar != null) {
            avatar.delete();
        }
    }

    public void block(final User user) {
        if (isBlocking(user)) {
            return;
        }

        final var block = new Block();
        block.source = this;
        block.target = user;
        block.persist();
        Subscription.delete("user = ?1 and post.author = ?2", user, this);
    }

    public void unblock(final User user) {
        Block.delete("source = ?1 and target = ?2", this, user);
    }

    public boolean isBlocking(final User user) {
        return Block.count("source = ?1 and target = ?2", this, user) > 0;
    }

    public void subscribeTo(final Post post) {
        if (isSubscribedTo(post)) {
            return;
        }

        final var subscription = new Subscription();
        subscription.user = this;
        subscription.post = post;
        subscription.lastCommentSeen =
                Comment.find("post", Comment.sorting().descending(), post).firstResult();
        subscription.persist();
    }

    public void unsubscribeFrom(final Post post) {
        Subscription.delete("user = ?1 and post = ?2", this, post);
    }

    public boolean isSubscribedTo(final Post post) {
        return Subscription.count("user = ?1 and post = ?2", this, post) > 0;
    }

    public static @Nullable User findByUsername(final String username) {
        return findByUsername(username, null);
    }

    public static @Nullable User findByUsername(final String username, @Nullable final LockModeType lock) {
        return User.<User>find("username", username).withLock(lock).firstResult();
    }

    public static User getFromSecurityContext(final SecurityContext context) {
        return getFromSecurityContext(context, null);
    }

    public static User getFromSecurityContext(final SecurityContext context, @Nullable final LockModeType lock) {
        return getFromSecurityContext(context, lock, true);
    }

    public static @Nullable User getFromSecurityContext(
            final SecurityContext context, @Nullable final LockModeType lock, final boolean required) {
        final var principal = context.getUserPrincipal();
        final var user = findByUsername(principal != null ? principal.getName() : "", lock);

        if (user == null && required) {
            throw new NotAuthorizedException("Bearer");
        }

        Sentry.configureScope(scope -> {
            if (user != null) {
                final var sentryUser = new io.sentry.protocol.User();
                sentryUser.setId(user.id.toString());
                sentryUser.setUsername(user.username);
                sentryUser.setEmail(user.mainEmail != null ? user.mainEmail.email : null);
                scope.setUser(sentryUser);
            } else {
                scope.setUser(null);
            }
        });

        return user;
    }

    public enum Rank {
        CITIZEN,
        MODERATOR,
        ADMINISTRATOR
    }

    public enum BanCount {
        NEVER,
        ONCE,
        ONE_TOO_MANY
    }

    public record Profile(
            @Schema(required = true) UUID id,
            @Schema(required = true) String username,
            @Schema(required = true) String avatar,
            @Schema(required = true) String tint) {}
}
