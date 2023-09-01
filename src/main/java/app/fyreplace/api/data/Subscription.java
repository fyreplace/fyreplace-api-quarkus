package app.fyreplace.api.data;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "subscriptions", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "post_id"}))
public class Subscription extends EntityBase {
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public User user;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Post post;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    public Comment lastCommentSeen;

    @Column(nullable = false)
    @UpdateTimestamp(source = SourceType.DB)
    public Instant dateLastSeen;
}
