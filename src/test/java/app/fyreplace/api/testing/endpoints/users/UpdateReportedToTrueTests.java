package app.fyreplace.api.testing.endpoints.users;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import app.fyreplace.api.data.Report;
import app.fyreplace.api.data.ReportUpdate;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.UsersEndpoint;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(UsersEndpoint.class)
public final class UpdateReportedToTrueTests extends UserTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void updateReport() {
        final var source = requireNonNull(User.findByUsername("user_0"));
        final var target = requireNonNull(User.findByUsername("user_1"));
        assertFalse(target.isReportedBy(source));
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
                .put(target.id + "/reported")
                .then()
                .statusCode(200);
        assertTrue(target.isReportedBy(source));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void updateReportTwice() {
        final var source = requireNonNull(User.findByUsername("user_0"));
        final var target = requireNonNull(User.findByUsername("user_1"));
        assertFalse(target.isReportedBy(source));
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
                .put(target.id + "/reported")
                .then()
                .statusCode(200);
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
                .put(target.id + "/reported")
                .then()
                .statusCode(200);
        assertTrue(target.isReportedBy(source));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void updateReportWithInactiveUser() {
        final var reportCount = Report.count();
        final var target = requireNonNull(User.findByUsername("user_inactive_1"));
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
                .put(target.id + "/reported")
                .then()
                .statusCode(404);
        assertEquals(reportCount, Report.count());
    }

    @Test
    @TestSecurity(user = "user_0")
    public void updateReportWithInvalidUser() {
        final var reportCount = Report.count();
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
                .put("invalid/reported")
                .then()
                .statusCode(404);
        assertEquals(reportCount, Report.count());
    }

    @Test
    @TestSecurity(user = "user_0")
    public void updateReportWithSelf() {
        final var user = requireNonNull(User.findByUsername("user_0"));
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
                .put(user.id + "/reported")
                .then()
                .statusCode(403);
        assertFalse(user.isReportedBy(user));
    }

    @Test
    public void updateReportUnauthenticated() {
        final var reportCount = Report.count();
        final var user = requireNonNull(User.findByUsername("user_0"));
        given().contentType(ContentType.JSON)
                .body(new ReportUpdate(true))
                .put(user.id + "/reported")
                .then()
                .statusCode(401);
        assertEquals(reportCount, Report.count());
    }
}
