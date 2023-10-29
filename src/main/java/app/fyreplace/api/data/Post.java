package app.fyreplace.api.data;

import app.fyreplace.api.exceptions.ForbiddenException;
import app.fyreplace.api.exceptions.GoneException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import io.quarkus.panache.common.Sort;
import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.ws.rs.NotFoundException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.hibernate.annotations.Formula;

@Entity
@Table(name = "posts")
public class Post extends AuthoredEntityBase implements Reportable {
    public boolean published = false;

    @Column(nullable = false)
    @JsonIgnore
    public int life = 0;

    @SuppressWarnings("unused")
    @Formula("(select count(*) from comments where comments.post_id = id)")
    public long commentCount;

    @SuppressWarnings("unused")
    @Formula("(select count(*) from votes where votes.post_id = id)")
    public long voteCount;

    public static Duration shelfLife = Duration.ofDays(7);

    @Override
    public void scrub() {
        super.scrub();
        getChapters().forEach(Chapter::delete);
    }

    public List<Chapter> getChapters() {
        return Chapter.list("post", Sort.by("position"), this);
    }

    @JsonIgnore
    public boolean isOld() {
        return published && Instant.now().isAfter(dateCreated.plus(shelfLife));
    }

    public void publish(final int life, final boolean anonymous) {
        dateCreated = Instant.now();
        published = true;
        this.life = life;
        this.anonymous = anonymous;
        persist();
        author.subscribeTo(this);
    }

    public void normalize() {
        final var chapters = getChapters();

        for (final var chapter : chapters) {
            chapter.position = chapter.position.replace("a", "0").replace("z", "9");
            chapter.persist();
        }

        for (var i = 0; i < chapters.size(); i++) {
            final var chapter = chapters.get(i);
            final var before = i - 1 >= 0 ? chapters.get(i - 1).position : null;
            chapter.position = Chapter.positionBetween(before, null);
            chapter.persist();
        }
    }

    public static void validateAccess(
            @Nullable final Post post,
            @Nullable final User user,
            @Nullable final Boolean mustBePublished,
            @Nullable final Boolean mustBeAuthor) {
        final Boolean postIsDraft = post != null && !post.published;
        final var userId = user != null ? user.id : null;

        if (post == null || (!post.author.id.equals(userId) && postIsDraft)) {
            throw new NotFoundException();
        } else if (post.deleted) {
            throw new GoneException();
        } else if (mustBePublished == postIsDraft) {
            throw new ForbiddenException(postIsDraft ? "post_not_published" : "post_is_published");
        } else if (mustBeAuthor != null && mustBeAuthor != post.author.id.equals(userId)) {
            throw new ForbiddenException("invalid_author");
        } else if (!post.anonymous && user != null && post.author.isBlocking(user)) {
            throw new ForbiddenException("user_is_blocked");
        }

        post.setCurrentUser(user);
    }
}
