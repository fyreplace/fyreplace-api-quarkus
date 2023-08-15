package app.fyreplace.api.testing.endpoints.users;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import app.fyreplace.api.data.StoredFile;
import app.fyreplace.api.data.User;
import app.fyreplace.api.endpoints.UsersEndpoint;
import app.fyreplace.api.testing.ImageTests;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestHTTPEndpoint(UsersEndpoint.class)
public final class UpdateMeAvatarTests extends ImageTests {
    @ParameterizedTest
    @ValueSource(strings = {"jpeg", "png", "webp"})
    @TestSecurity(user = "user_0")
    public void updateMeAvatar(final String fileType) throws IOException {
        final var remoteFileCount = StoredFile.count();

        try (final var stream = openStream(fileType)) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .put("me/avatar")
                    .then()
                    .contentType(ContentType.TEXT)
                    .statusCode(200)
                    .body(isA(String.class));
        }

        assertEquals(remoteFileCount + 1, StoredFile.count());
        final var user = User.findByUsername("user_0");
        assertNotNull(user.avatar);
    }

    @ParameterizedTest
    @ValueSource(strings = {"gif", "text"})
    @TestSecurity(user = "user_0")
    public void updateMeAvatarWithInvalidType(final String fileType) throws IOException {
        final var remoteFileCount = StoredFile.count();

        try (final var stream = openStream(fileType)) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .put("me/avatar")
                    .then()
                    .statusCode(415);
        }

        assertEquals(remoteFileCount, StoredFile.count());
        final var user = User.findByUsername("user_0");
        assertNull(user.avatar);
    }

    @Test
    @TestSecurity(user = "user_0")
    public void updateMeAvatarWithoutInput() {
        final var remoteFileCount = StoredFile.count();
        given().contentType(ContentType.BINARY).put("me/avatar").then().statusCode(415);
        assertEquals(remoteFileCount, StoredFile.count());
        final var user = User.findByUsername("user_0");
        assertNull(user.avatar);
    }
}
