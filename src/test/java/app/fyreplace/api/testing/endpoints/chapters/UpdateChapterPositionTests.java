package app.fyreplace.api.testing.endpoints.chapters;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.Chapter;
import app.fyreplace.api.endpoints.ChaptersEndpoint;
import app.fyreplace.api.testing.PostTestsBase;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestHTTPEndpoint(ChaptersEndpoint.class)
public final class UpdateChapterPositionTests extends PostTestsBase {
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @TestSecurity(user = "user_0")
    @Transactional
    public void updateChapterPositionInOwnPost(final int to) {
        final var from = 1;
        final var chapter = post.getChapters().get(from);
        final var position = chapter.position;
        given().body(to).pathParam("id", post.id).put(from + "/position").then().statusCode(403);
        chapter.refresh();
        assertEquals(position, chapter.position);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @TestSecurity(user = "user_0")
    public void updateChapterPositionInOwnDraft(final int to) {
        final var from = 1;
        final var chapter = draft.getChapters().get(from);
        given().body(to)
                .pathParam("id", draft.id)
                .put(from + "/position")
                .then()
                .statusCode(200);
        final var chapters = Chapter.<Chapter>list("post", Sort.by("position"), draft);
        assertEquals(chapter.id, chapters.get(to).id);
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "12"})
    @TestSecurity(user = "user_0")
    @Transactional
    public void updateChapterPositionInOwnDraftOutOfBounds(final String to) {
        final var from = 1;
        final var chapter = draft.getChapters().get(from);
        final var position = chapter.position;
        given().body(to)
                .pathParam("id", draft.id)
                .put(from + "/position")
                .then()
                .statusCode(404);
        chapter.refresh();
        assertEquals(position, chapter.position);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1.5", "null"})
    @TestSecurity(user = "user_0")
    @Transactional
    public void updateChapterPositionInOwnDraftWithInvalidInput(final String to) {
        final var from = 1;
        final var chapter = draft.getChapters().get(from);
        final var position = chapter.position;
        given().body(to)
                .pathParam("id", draft.id)
                .put(from + "/position")
                .then()
                .statusCode(400);
        chapter.refresh();
        assertEquals(position, chapter.position);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @TestSecurity(user = "user_1")
    @Transactional
    public void updateChapterPositionInOtherPost(final int to) {
        final var from = 1;
        final var chapter = post.getChapters().get(from);
        final var oldPosition = chapter.position;
        given().body(to).pathParam("id", post.id).put(from + "/position").then().statusCode(403);
        chapter.refresh();
        assertEquals(oldPosition, chapter.position);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @TestSecurity(user = "user_1")
    @Transactional
    public void updateChapterPositionInOtherDraft(final int to) {
        final var from = 1;
        final var chapter = draft.getChapters().get(from);
        final var oldPosition = chapter.position;
        given().body(to)
                .pathParam("id", draft.id)
                .put(from + "/position")
                .then()
                .statusCode(404);
        chapter.refresh();
        assertEquals(oldPosition, chapter.position);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @Transactional
    public void updateChapterPositionInPostUnauthenticated(final int to) {
        final var from = 1;
        final var chapter = post.getChapters().get(from);
        final var oldPosition = chapter.position;
        given().body(to).pathParam("id", post.id).put(from + "/position").then().statusCode(401);
        chapter.refresh();
        assertEquals(oldPosition, chapter.position);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @Transactional
    public void updateChapterPositionInDraftUnauthenticated(final int to) {
        final var from = 1;
        final var chapter = draft.getChapters().get(from);
        final var oldPosition = chapter.position;
        given().body(to)
                .pathParam("id", draft.id)
                .put(from + "/position")
                .then()
                .statusCode(401);
        chapter.refresh();
        assertEquals(oldPosition, chapter.position);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @TestSecurity(user = "user_0")
    @Transactional
    public void updateChapterPositionInNonExistentPost(final int to) {
        final var from = 1;
        final var chapter = post.getChapters().get(from);
        final var oldPosition = chapter.position;
        given().body(to).pathParam("id", fakeId).put(from + "/position").then().statusCode(404);
        chapter.refresh();
        assertEquals(oldPosition, chapter.position);
    }

    @ParameterizedTest
    @ValueSource(strings = {"fake", "null"})
    @TestSecurity(user = "user_0")
    @Transactional
    public void updateNonExistentChapterPosition(final String from) {
        given().body(1).pathParam("id", draft.id).put(from + "/position").then().statusCode(404);
    }
}
