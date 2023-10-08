package app.fyreplace.api.testing.endpoints.emails;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.range;
import static org.hamcrest.Matchers.equalTo;

import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.EmailsEndpoint;
import app.fyreplace.api.testing.UserTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(EmailsEndpoint.class)
public class CountTests extends UserTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void count() {
        given().get("count")
                .then()
                .statusCode(200)
                .body(equalTo(String.valueOf(Email.count("user.username = 'user_0'"))));
    }

    @BeforeEach
    @Transactional
    @Override
    public void beforeEach() {
        super.beforeEach();
        final var user = requireNonNull(User.findByUsername("user_0"));
        range(0, 30).forEach(i -> {
            final var email = new Email();
            email.user = user;
            email.email = user.username + "_" + i + "@example.com";
            email.verified = i % 5 == 0;
            email.persist();
        });
    }
}
