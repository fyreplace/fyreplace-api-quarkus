package app.fyreplace.api.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(
        name = "comments",
        indexes = {@Index(columnList = "post_id")})
public class Comment extends AuthoredEntityBase implements Comparable<Comment> {
    @ManyToOne(optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    public Post post;

    @Column(length = 1500, nullable = false)
    public String text;

    @Column(nullable = false)
    public boolean deleted;

    @Override
    public int compareTo(@Nonnull final Comment other) {
        final var dateComparison = dateCreated.compareTo(other.dateCreated);
        return dateComparison != 0 ? dateComparison : id.compareTo(other.id);
    }

    public void softDelete() {
        text = "";
        deleted = true;
        persist();
    }
}
