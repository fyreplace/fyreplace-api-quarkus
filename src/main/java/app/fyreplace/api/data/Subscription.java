package app.fyreplace.api.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import org.hibernate.annotations.Formula;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "subscriptions", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"}))
public class Subscription extends EntityBase {
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    public User user;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Post post;

    @SuppressWarnings("unused")
    @Column(nullable = false)
    @UpdateTimestamp(source = SourceType.DB)
    @JsonIgnore
    public Instant dateUpdated;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonIgnore
    public Comment lastCommentSeen;

    @SuppressWarnings("unused")
    @Formula(
            """
            (
                select count(*) from comments
                where comments.post_id = post_id
                and (
                    case
                        when last_comment_seen_id is null then true
                        else (
                            comments.date_created > (
                                select comments.date_created from comments
                                where comments.id = last_comment_seen_id
                            )
                        )
                    end
                )
            )
            """)
    public long unreadCommentCount;

    public void markAsRead() {
        lastCommentSeen =
                Comment.find("post", Post.sorting().descending(), post).firstResult();
        persist();
    }

    public static Sort sorting() {
        return Sort.by("dateUpdated", "id");
    }

    public static void markAsRead(final User user) {
        update(
                """
                update Subscription as s set
                lastCommentSeen = (
                    select id from Comment as c
                    where c.post = s.post
                    order by c.dateCreated desc, c.id desc
                    limit 1
                )
                where user = ?1
                """,
                user);
    }
}
