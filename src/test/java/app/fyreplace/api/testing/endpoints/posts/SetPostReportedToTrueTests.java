package app.fyreplace.api.testing.endpoints.posts;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.fyreplace.api.data.Report;
import app.fyreplace.api.data.ReportUpdate;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.PostsEndpoint;
import app.fyreplace.api.testing.PostTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestHTTPEndpoint(PostsEndpoint.class)
public final class SetPostReportedToTrueTests extends PostTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void setOwnPostReported() {
        final var user = User.findByUsername("user_0");
        assertFalse(post.isReportedBy(user));
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
                .put(post.id + "/reported")
                .then()
                .statusCode(403);
        assertFalse(post.isReportedBy(user));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void setOtherPostReported() {
        final var user = User.findByUsername("user_1");
        assertFalse(post.isReportedBy(user));
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
                .put(post.id + "/reported")
                .then()
                .statusCode(200);
        assertTrue(post.isReportedBy(user));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void setOtherPostReportedTwice() {
        final var user = User.findByUsername("user_1");
        assertFalse(post.isReportedBy(user));
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
                .put(post.id + "/reported")
                .then()
                .statusCode(200);
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
                .put(post.id + "/reported")
                .then()
                .statusCode(200);
        assertTrue(post.isReportedBy(user));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void setOwnDraftReported() {
        final var user = User.findByUsername("user_0");
        assertFalse(draft.isReportedBy(user));
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
                .put(draft.id + "/reported")
                .then()
                .statusCode(403);
        assertFalse(draft.isReportedBy(user));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void setOtherDraftReported() {
        final var user = User.findByUsername("user_1");
        assertFalse(draft.isReportedBy(user));
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
                .put(draft.id + "/reported")
                .then()
                .statusCode(404);
        assertFalse(draft.isReportedBy(user));
    }

    @ParameterizedTest
    @ValueSource(strings = {"fake", "00000000-0000-0000-0000-000000000000"})
    @TestSecurity(user = "user_0")
    public void setNonExistentPostReported(final String id) {
        final var reportCount = Report.count();
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
                .put(id + "/reported")
                .then()
                .statusCode(404);
        assertEquals(reportCount, Report.count());
    }

    @Test
    public void setPostReportedWhileWhileUnauthenticated() {
        final var reportCount = Report.count();
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
                .put(post.id + "/reported")
                .then()
                .statusCode(401);
        assertEquals(reportCount, Report.count());
    }
}
