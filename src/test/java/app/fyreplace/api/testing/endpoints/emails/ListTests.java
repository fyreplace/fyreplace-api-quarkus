package app.fyreplace.api.testing.endpoints.emails;

import static io.restassured.RestAssured.given;
import static java.util.Objects.requireNonNull;
import static java.util.stream.IntStream.range;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.in;

import app.fyreplace.api.data.Email;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.EmailsEndpoint;
import app.fyreplace.api.testing.TransactionalTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.transaction.Transactional;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(EmailsEndpoint.class)
public final class ListTests extends TransactionalTestsBase {
    @ConfigProperty(name = "app.paging.size")
    int pagingSize;

    @Test
    @TestSecurity(user = "user_0")
    public void list() {
        final var user = User.findByUsername("user_0");
        final var response = given().queryParam("page", 0)
                .get()
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(pagingSize));

        try (final var stream = Email.<Email>stream("user", user)) {
            final var emails = stream.map(email -> email.email).toList();
            range(0, pagingSize).forEach(i -> response.body("[" + i + "].email", in(emails)));
        }
    }

    @Test
    @TestSecurity(user = "user_0")
    public void listOutOfBounds() {
        given().queryParam("page", -1).get().then().statusCode(400);
    }

    @Test
    @TestSecurity(user = "user_0")
    public void listTooFar() {
        given().queryParam("page", 50)
                .get()
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body(equalTo("[]"));
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
