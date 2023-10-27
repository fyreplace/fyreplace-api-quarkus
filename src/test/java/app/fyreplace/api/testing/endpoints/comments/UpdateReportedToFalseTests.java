package app.fyreplace.api.testing.endpoints.comments;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.fyreplace.api.data.Comment;
import app.fyreplace.api.data.Report;
import app.fyreplace.api.data.ReportUpdate;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.CommentsEndpoint;
import app.fyreplace.api.testing.CommentTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(CommentsEndpoint.class)
public final class UpdateReportedToFalseTests extends CommentTestsBase {
    private Comment comment;

    @Test
    @TestSecurity(user = "user_0")
    public void updateReportWithOwnComment() {
        final var user = User.findByUsername("user_0");
        assertFalse(comment.isReportedBy(user));
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(false))
                .pathParam("id", post.id)
                .put("0/reported")
                .then()
                .statusCode(403);
        assertFalse(comment.isReportedBy(user));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void updateReportWithOtherComment() {
        final var user = User.findByUsername("user_1");
        assertTrue(comment.isReportedBy(user));
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(false))
                .pathParam("id", post.id)
                .put("0/reported")
                .then()
                .statusCode(200);
        assertFalse(comment.isReportedBy(user));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void updateReportWithOtherCommentTwice() {
        final var user = User.findByUsername("user_1");
        assertTrue(comment.isReportedBy(user));
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(false))
                .pathParam("id", post.id)
                .put("0/reported")
                .then()
                .statusCode(200);
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(false))
                .pathParam("id", post.id)
                .put("0/reported")
                .then()
                .statusCode(200);
        assertFalse(comment.isReportedBy(user));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void updateReportOutOfBounds() {
        final var reportCount = Report.count();
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(false))
                .pathParam("id", post.id)
                .put("-1/reported")
                .then()
                .statusCode(400);
        assertEquals(reportCount, Report.count());
    }

    @Test
    @TestSecurity(user = "user_0")
    public void updateReportTooFar() {
        final var reportCount = Report.count();
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(false))
                .pathParam("id", post.id)
                .put("50/reported")
                .then()
                .statusCode(404);
        assertEquals(reportCount, Report.count());
    }

    @Test
    public void updateReportUnauthenticated() {
        final var reportCount = Report.count();
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(false))
                .pathParam("id", post.id)
                .put("0/reported")
                .then()
                .statusCode(401);
        assertEquals(reportCount, Report.count());
    }

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        final var user = requireNonNull(User.findByUsername("user_1"));
        comment = Comment.find("post = ?1 and author.username = 'user_0'", Comment.sorting(), post)
                .firstResult();
        comment.reportBy(user);
    }
}
