package app.fyreplace.api.testing.endpoints.chapters;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import app.fyreplace.api.data.Chapter;
import app.fyreplace.api.endpoints.ChaptersEndpoint;
import app.fyreplace.api.testing.endpoints.PostTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestHTTPEndpoint(ChaptersEndpoint.class)
public final class UpdateChapterImageTests extends PostTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void updateChapterImageInOwnPost() throws IOException {
        final var position = 0;

        try (final var stream = openStream("jpeg")) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .pathParam("id", post.id)
                    .put(position + "/image")
                    .then()
                    .statusCode(403);
        }

        final var chapter = post.getChapters().get(position);
        assertHasNoImage(chapter);
    }

    @ParameterizedTest
    @ValueSource(strings = {"jpeg", "png", "webp"})
    @TestSecurity(user = "user_0")
    public void updateChapterImageInOwnDraft(final String fileType) throws IOException {
        final var position = 0;

        try (final var stream = openStream(fileType)) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .pathParam("id", draft.id)
                    .put(position + "/image")
                    .then()
                    .statusCode(200);
        }

        final var chapter = draft.getChapters().get(position);
        assertHasImage(chapter);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "12"})
    @TestSecurity(user = "user_0")
    public void updateChapterImageInOwnDraftOutOfBounds(final String position) throws IOException {
        try (final var stream = openStream("jpeg")) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .pathParam("id", draft.id)
                    .put(position + "/image")
                    .then()
                    .statusCode(404);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"gif", "text"})
    @TestSecurity(user = "user_0")
    public void updateChapterImageInOwnDraftWithInvalidType(final String fileType) throws IOException {
        final var position = 0;

        try (final var stream = openStream(fileType)) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .pathParam("id", draft.id)
                    .put(position + "/image")
                    .then()
                    .statusCode(415);
        }

        final var chapter = draft.getChapters().get(position);
        assertHasNoImage(chapter);
    }

    @Test
    @TestSecurity(user = "user_0")
    public void updateChapterImageInOwnDraftWithoutInput() {
        final var position = 0;
        given().pathParam("id", draft.id).put(position + "/image").then().statusCode(415);
        final var chapter = draft.getChapters().get(position);
        assertHasNoImage(chapter);
    }

    @Test
    @TestSecurity(user = "user_1")
    public void updateChapterImageInOtherPost() throws IOException {
        final var position = 0;

        try (final var stream = openStream("jpeg")) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .pathParam("id", post.id)
                    .put(position + "/image")
                    .then()
                    .statusCode(403);
        }

        final var chapter = post.getChapters().get(position);
        assertHasNoImage(chapter);
    }

    @Test
    @TestSecurity(user = "user_1")
    public void updateChapterImageInOtherDraft() throws IOException {
        final var position = 0;

        try (final var stream = openStream("jpeg")) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .pathParam("id", draft.id)
                    .put(position + "/image")
                    .then()
                    .statusCode(404);
        }

        final var chapter = post.getChapters().get(position);
        assertHasNoImage(chapter);
    }

    @Test
    public void updateChapterImageInPostUnauthenticated() throws IOException {
        final var position = 0;

        try (final var stream = openStream("jpeg")) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .pathParam("id", post.id)
                    .put(position + "/image")
                    .then()
                    .statusCode(401);
        }

        final var chapter = post.getChapters().get(position);
        assertHasNoImage(chapter);
    }

    @Test
    public void updateChapterImageInDraftUnauthenticated() throws IOException {
        final var position = 0;

        try (final var stream = openStream("jpeg")) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .pathParam("id", draft.id)
                    .put(position + "/image")
                    .then()
                    .statusCode(401);
        }

        final var chapter = draft.getChapters().get(position);
        assertHasNoImage(chapter);
    }

    @Test
    @TestSecurity(user = "user_0")
    public void updateChapterTextInNonExistentPost() throws IOException {
        final var position = 0;

        try (final var stream = openStream("jpeg")) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .pathParam("id", fakeId)
                    .put(position + "/image")
                    .then()
                    .statusCode(404);
        }

        final var chapter = draft.getChapters().get(position);
        assertHasNoImage(chapter);
    }

    @ParameterizedTest
    @ValueSource(strings = {"fake", "null"})
    @TestSecurity(user = "user_0")
    public void updateNonExistentChapterText(final String position) throws IOException {
        try (final var stream = openStream("jpeg")) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .pathParam("id", draft.id)
                    .put(position + "/image")
                    .then()
                    .statusCode(404);
        }
    }

    private void assertHasImage(final Chapter chapter) {
        assertNotNull(chapter.image);
        assertEquals(256, chapter.width);
        assertEquals(256, chapter.height);
    }

    private void assertHasNoImage(final Chapter chapter) {
        assertNull(chapter.image);
        assertEquals(0, chapter.width);
        assertEquals(0, chapter.height);
    }
}
