package app.fyreplace.api.testing.endpoints.chapters;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.endpoints.ChaptersEndpoint;
import app.fyreplace.api.testing.PostTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestHTTPEndpoint(ChaptersEndpoint.class)
public final class UpdateChapterTextTests extends PostTestsBase {
    @Test
    @TestSecurity(user = "user_0")
    @Transactional
    public void updateChapterTextInOwnPost() {
        final var position = 0;
        final var chapter = post.getChapters().getFirst();
        final var oldText = chapter.text;
        given().body("Hello")
                .pathParam("id", post.id)
                .put(position + "/text")
                .then()
                .statusCode(403);
        chapter.refresh();
        assertEquals(oldText, chapter.text);
    }

    @ParameterizedTest
    @ValueSource(strings = {"Hello", ""})
    @TestSecurity(user = "user_0")
    public void updateChapterTextInOwnDraft(final String text) {
        final var position = 0;
        given().body(text)
                .pathParam("id", draft.id)
                .put(position + "/text")
                .then()
                .statusCode(200);
        final var chapter = draft.getChapters().getFirst();
        assertEquals(text, chapter.text);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "12"})
    @TestSecurity(user = "user_0")
    public void updateChapterTextInOwnDraftOutOfBounds(final String position) {
        given().body("Hello")
                .pathParam("id", draft.id)
                .put(position + "/text")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = "user_0")
    @Transactional
    public void updateChapterTextInOwnDraftWithInvalidInput() {
        final var position = 0;
        final var chapter = draft.getChapters().getFirst();
        final var oldText = chapter.text;
        given().body("a".repeat(501))
                .pathParam("id", draft.id)
                .put(position + "/text")
                .then()
                .statusCode(400);
        chapter.refresh();
        assertEquals(oldText, chapter.text);
    }

    @Test
    @TestSecurity(user = "user_1")
    @Transactional
    public void updateChapterTextInOtherPost() {
        final var position = 0;
        final var chapter = post.getChapters().getFirst();
        final var oldText = chapter.text;
        given().body("Hello")
                .pathParam("id", post.id)
                .put(position + "/text")
                .then()
                .statusCode(403);
        chapter.refresh();
        assertEquals(oldText, chapter.text);
    }

    @Test
    @TestSecurity(user = "user_1")
    @Transactional
    public void updateChapterTextInOtherDraft() {
        final var position = 0;
        final var chapter = draft.getChapters().getFirst();
        final var oldText = chapter.text;
        given().body("Hello")
                .pathParam("id", draft.id)
                .put(position + "/text")
                .then()
                .statusCode(404);
        chapter.refresh();
        assertEquals(oldText, chapter.text);
    }

    @Test
    @Transactional
    public void updateChapterTextInPostUnauthenticated() {
        final var position = 0;
        final var chapter = post.getChapters().getFirst();
        final var oldText = chapter.text;
        given().body("Hello")
                .pathParam("id", post.id)
                .put(position + "/text")
                .then()
                .statusCode(401);
        chapter.refresh();
        assertEquals(oldText, chapter.text);
    }

    @Test
    @Transactional
    public void updateChapterTextInDraftUnauthenticated() {
        final var position = 0;
        final var chapter = draft.getChapters().getFirst();
        final var oldText = chapter.text;
        given().body("Hello")
                .pathParam("id", draft.id)
                .put(position + "/text")
                .then()
                .statusCode(401);
        chapter.refresh();
        assertEquals(oldText, chapter.text);
    }

    @Test
    @TestSecurity(user = "user_0")
    @Transactional
    public void updateChapterTextInNonExistentPost() {
        final var position = 0;
        final var chapter = draft.getChapters().getFirst();
        final var oldText = chapter.text;
        given().body("Hello")
                .pathParam("id", fakeId)
                .put(position + "/text")
                .then()
                .statusCode(404);
        chapter.refresh();
        assertEquals(oldText, chapter.text);
    }

    @ParameterizedTest
    @ValueSource(strings = {"fake", "null"})
    @TestSecurity(user = "user_0")
    public void updateNonExistentChapterText(final String from) {
        given().body("Hello")
                .pathParam("id", draft.id)
                .put(from + "/text")
                .then()
                .statusCode(404);
    }
}
