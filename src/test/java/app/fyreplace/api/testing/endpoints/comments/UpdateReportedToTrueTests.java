package app.fyreplace.api.testing.endpoints.comments;

import static io.restassured.RestAssured.given;
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
public final class UpdateReportedToTrueTests extends CommentTestsBase {
    private Comment comment;

    @Test
    @TestSecurity(user = "user_0")
    public void setReportWithOwnComment() {
        final var user = User.findByUsername("user_0");
        assertFalse(comment.isReportedBy(user));
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
                .pathParam("id", post.id)
                .put("0/reported")
                .then()
                .statusCode(403);
        assertFalse(comment.isReportedBy(user));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void setReportWithOtherComment() {
        final var user = User.findByUsername("user_1");
        assertFalse(comment.isReportedBy(user));
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
                .pathParam("id", post.id)
                .put("0/reported")
                .then()
                .statusCode(200);
        assertTrue(comment.isReportedBy(user));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void setReportWithOtherCommentTwice() {
        final var user = User.findByUsername("user_1");
        assertFalse(comment.isReportedBy(user));
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
                .pathParam("id", post.id)
                .put("0/reported")
                .then()
                .statusCode(200);
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
                .pathParam("id", post.id)
                .put("0/reported")
                .then()
                .statusCode(200);
        assertTrue(comment.isReportedBy(user));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void setReportOutOfBounds() {
        final var reportCount = Report.count();
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
                .pathParam("id", post.id)
                .put("-1/reported")
                .then()
                .statusCode(400);
        assertEquals(reportCount, Report.count());
    }

    @Test
    @TestSecurity(user = "user_0")
    public void setReportTooFar() {
        final var reportCount = Report.count();
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
                .pathParam("id", post.id)
                .put("50/reported")
                .then()
                .statusCode(404);
        assertEquals(reportCount, Report.count());
    }

    @Test
    public void setReportWhileUnauthenticated() {
        final var reportCount = Report.count();
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
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
        comment = Comment.find("post = ?1 and author.username = 'user_0'", Comment.sorting(), post)
                .firstResult();
    }
}
