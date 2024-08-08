package app.fyreplace.api.testing.endpoints.chapters;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import app.fyreplace.api.data.Chapter;
import app.fyreplace.api.endpoints.ChaptersEndpoint;
import app.fyreplace.api.testing.PostTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestHTTPEndpoint(ChaptersEndpoint.class)
public final class SetChapterImageTests extends PostTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    public void setChapterImageInOwnPost() throws IOException {
        final var position = 0;

        try (final var stream = openStream("jpeg")) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .pathParam("id", post.id)
                    .put(position + "/image")
                    .then()
                    .statusCode(403);
        }

        final var chapter = post.getChapters().getFirst();
        assertHasNoImage(chapter);
    }

    @ParameterizedTest
    @ValueSource(strings = {"jpeg", "png", "webp"})
    @TestSecurity(user = "user_0")
    public void setChapterImageInOwnDraft(final String fileType) throws IOException {
        final var position = 0;

        try (final var stream = openStream(fileType)) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .pathParam("id", draft.id)
                    .put(position + "/image")
                    .then()
                    .statusCode(200);
        }

        final var chapter = draft.getChapters().getFirst();
        assertHasImage(chapter);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "12", "1.5", "null"})
    @TestSecurity(user = "user_0")
    public void setChapterImageInOwnDraftOutOfBounds(final String position) throws IOException {
        try (final var stream = openStream("jpeg")) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .pathParam("id", draft.id)
                    .put(position + "/image")
                    .then()
                    .statusCode(anyOf(List.of(equalTo(400), equalTo(404))));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"gif", "text"})
    @TestSecurity(user = "user_0")
    public void setChapterImageInOwnDraftWithInvalidType(final String fileType) throws IOException {
        final var position = 0;

        try (final var stream = openStream(fileType)) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .pathParam("id", draft.id)
                    .put(position + "/image")
                    .then()
                    .statusCode(415);
        }

        final var chapter = draft.getChapters().getFirst();
        assertHasNoImage(chapter);
    }

    @Test
    @TestSecurity(user = "user_0")
    public void setChapterImageInOwnDraftWithoutInput() {
        final var position = 0;
        given().pathParam("id", draft.id).put(position + "/image").then().statusCode(415);
        final var chapter = draft.getChapters().getFirst();
        assertHasNoImage(chapter);
    }

    @Test
    @TestSecurity(user = "user_1")
    public void setChapterImageInOtherPost() throws IOException {
        final var position = 0;

        try (final var stream = openStream("jpeg")) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .pathParam("id", post.id)
                    .put(position + "/image")
                    .then()
                    .statusCode(403);
        }

        final var chapter = post.getChapters().getFirst();
        assertHasNoImage(chapter);
    }

    @Test
    @TestSecurity(user = "user_1")
    public void setChapterImageInOtherDraft() throws IOException {
        final var position = 0;

        try (final var stream = openStream("jpeg")) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .pathParam("id", draft.id)
                    .put(position + "/image")
                    .then()
                    .statusCode(404);
        }

        final var chapter = post.getChapters().getFirst();
        assertHasNoImage(chapter);
    }

    @Test
    public void setChapterImageInPostWhileUnauthenticated() throws IOException {
        final var position = 0;

        try (final var stream = openStream("jpeg")) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .pathParam("id", post.id)
                    .put(position + "/image")
                    .then()
                    .statusCode(401);
        }

        final var chapter = post.getChapters().getFirst();
        assertHasNoImage(chapter);
    }

    @Test
    public void setChapterImageInDraftWhileUnauthenticated() throws IOException {
        final var position = 0;

        try (final var stream = openStream("jpeg")) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .pathParam("id", draft.id)
                    .put(position + "/image")
                    .then()
                    .statusCode(401);
        }

        final var chapter = draft.getChapters().getFirst();
        assertHasNoImage(chapter);
    }

    @Test
    @TestSecurity(user = "user_0")
    public void setChapterTextInNonExistentPost() throws IOException {
        final var position = 0;

        try (final var stream = openStream("jpeg")) {
            given().contentType(ContentType.BINARY)
                    .body(stream.readAllBytes())
                    .pathParam("id", fakeId)
                    .put(position + "/image")
                    .then()
                    .statusCode(404);
        }

        final var chapter = draft.getChapters().getFirst();
        assertHasNoImage(chapter);
    }

    @ParameterizedTest
    @ValueSource(strings = {"fake", "null"})
    @TestSecurity(user = "user_0")
    public void setNonExistentChapterText(final String position) throws IOException {
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
