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
public final class SetChapterPositionTests extends PostTestsBase {
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @TestSecurity(user = "user_0")
    @Transactional
    public void setChapterPositionInOwnPost(final int to) {
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
    public void setChapterPositionInOwnDraft(final int to) {
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
    public void setChapterPositionInOwnDraftOutOfBounds(final String to) {
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
    public void setChapterPositionInOwnDraftWithInvalidInput(final String to) {
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
    public void setChapterPositionInOtherPost(final int to) {
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
    public void setChapterPositionInOtherDraft(final int to) {
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
    public void setChapterPositionInPostUnauthenticated(final int to) {
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
    public void setChapterPositionInDraftUnauthenticated(final int to) {
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
    public void setChapterPositionInNonExistentPost(final int to) {
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
    public void setNonExistentChapterPosition(final String from) {
        given().body(1).pathParam("id", draft.id).put(from + "/position").then().statusCode(404);
    }
}
