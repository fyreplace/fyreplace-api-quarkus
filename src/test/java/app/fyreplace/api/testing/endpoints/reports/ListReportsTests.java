package app.fyreplace.api.testing.endpoints.reports;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.range;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.in;

import app.fyreplace.api.data.Report;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.ReportsEndpoint;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(ReportsEndpoint.class)
public final class ListReportsTests extends UserTestsBase {
    @ConfigProperty(name = "app.paging.size")
    int pagingSize;

    @Test
    @TestSecurity(user = "user_0")
    public void listReportsAsCitizen() {
        given().get().then().statusCode(403);
    }

    @Test
    @TestSecurity(user = "user_0", roles = "MODERATOR")
    public void listReportsAsModerator() {
        final var response = given().get().then().statusCode(200).body("size()", equalTo(pagingSize));
        range(0, pagingSize).forEach(i -> {
            try (final var stream = Report.<Report>streamAll().map(r -> r.targetId.toString())) {
                response.body("[" + i + "].targetModel", equalTo(User.class.getSimpleName()))
                        .body("[" + i + "].targetId", in(stream.toList()));
            }
        });
    }

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        final var source = requireNonNull(User.findByUsername("user_0"));
        range(1, 16)
                .forEach(i -> requireNonNull(User.findByUsername("user_" + i)).reportBy(source));
    }

    @Override
    public int getActiveUserCount() {
        return 20;
    }
}
