package app.fyreplace.api.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.annotation.Nullable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostRemove;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "chapters", uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "position"}))
public class Chapter extends EntityBase {
    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    public Post post;

    @Column(length = 50, nullable = false)
    @JsonIgnore
    public String position;

    @Column(length = 500, nullable = false)
    public String text = "";

    @OneToOne(cascade = CascadeType.PERSIST)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JsonSerialize(using = StoredFile.Serializer.class)
    @Schema(implementation = String.class)
    public StoredFile image;

    @Column(nullable = false)
    public int width = 0;

    @Column(nullable = false)
    public int height = 0;

    @SuppressWarnings("unused")
    @PostRemove
    final void postRemove() {
        if (image != null) {
            image.delete();
        }
    }

    public static String positionBetween(final @Nullable String before, final @Nullable String after) {
        final var beforeLength = before == null ? 0 : before.length();
        final var afterLength = after == null ? 0 : after.length();

        if (before == null && after == null) {
            return "z";
        } else if (before != null && after != null && (before.equals(after) || before.compareTo(after) > 0)) {
            throw new IllegalArgumentException();
        } else if (after == null || beforeLength > afterLength) {
            return before + "z";
        } else {
            return after.substring(0, afterLength - 1) + "az";
        }
    }
}
