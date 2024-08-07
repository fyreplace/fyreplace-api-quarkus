package app.fyreplace.api.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "comments",
        indexes = {@Index(columnList = "post_id")})
public class Comment extends AuthoredEntityBase implements Comparable<Comment>, Reportable {
    public static final int TEXT_MAX_LENGTH = 1500;

    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    public Post post;

    @Column(length = TEXT_MAX_LENGTH, nullable = false)
    @Schema(required = true, maxLength = TEXT_MAX_LENGTH)
    public String text;

    @Override
    public int compareTo(@Nonnull final Comment other) {
        final var dateComparison = dateCreated.compareTo(other.dateCreated);
        return dateComparison != 0 ? dateComparison : id.compareTo(other.id);
    }

    @Override
    public void scrub() {
        super.scrub();
        text = "";
    }

    @SuppressWarnings("unused")
    @JsonProperty("deleted")
    public boolean isDeleted() {
        return deleted;
    }
}
