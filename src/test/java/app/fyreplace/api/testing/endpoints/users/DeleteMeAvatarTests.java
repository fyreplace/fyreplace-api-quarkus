package app.fyreplace.api.testing.endpoints.users;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.StoredFile;
import app.fyreplace.api.endpoints.UsersEndpoint;
import app.fyreplace.api.testing.TransactionalTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.io.IOException;
import java.net.URL;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(UsersEndpoint.class)
public final class DeleteMeAvatarTests extends TransactionalTestsBase {
    @TestHTTPResource("image.jpeg")
    URL jpeg;

    @Test
    @TestSecurity(user = "user_0")
    public void deleteMeAvatar() throws IOException {
        try (final var stream = jpeg.openStream()) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .put("me/avatar")
                    .then()
                    .statusCode(200);
        }

        final var remoteFileCount = StoredFile.count();
        given().delete("me/avatar").then().statusCode(204);
        assertEquals(remoteFileCount - 1, StoredFile.count());
    }

    @Test
    @TestSecurity(user = "user_0")
    public void deleteMeAvatarWithoutAvatar() {
        final var remoteFileCount = StoredFile.count();
        given().delete("me/avatar").then().statusCode(204);
        assertEquals(remoteFileCount, StoredFile.count());
    }
}
