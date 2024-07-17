package app.fyreplace.api.testing.endpoints.chapters;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.Chapter;
import app.fyreplace.api.endpoints.ChaptersEndpoint;
import app.fyreplace.api.testing.PostTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.transaction.Transactional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@QuarkusTest
@TestHTTPEndpoint(ChaptersEndpoint.class)
public final class DeleteChapterTests extends PostTestsBase {
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @TestSecurity(user = "user_0")
    public void deleteChapterInOwnPost(final int position) {
        final var chapterCount = Chapter.count("post", post);
        final var chapterId = post.getChapters().get(position).id;
        given().pathParam("id", post.id).delete(String.valueOf(position)).then().statusCode(403);
        assertEquals(chapterCount, Chapter.count("post", post));
        assertEquals(1, Chapter.count("id", chapterId));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @TestSecurity(user = "user_0")
    public void deleteChapterInOwnDraft(final int position) {
        final var chapterCount = Chapter.count("post", draft);
        final var chapterId = draft.getChapters().get(position).id;
        given().pathParam("id", draft.id)
                .delete(String.valueOf(position))
                .then()
                .statusCode(204);
        assertEquals(chapterCount - 1, Chapter.count("post", draft));
        assertEquals(0, Chapter.count("id", chapterId));
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "12", "1.5", "null"})
    @TestSecurity(user = "user_0")
    public void deleteChapterInOwnDraftOutOfBounds(final String position) {
        final var chapterCount = Chapter.count("post", draft);
        given().pathParam("id", draft.id)
                .delete(String.valueOf(position))
                .then()
                .statusCode(404);
        assertEquals(chapterCount, Chapter.count("post", draft));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @TestSecurity(user = "user_1")
    public void deleteChapterInOtherPost(final int position) {
        final var chapterCount = Chapter.count("post", post);
        final var chapterId = post.getChapters().get(position).id;
        given().pathParam("id", post.id).delete(String.valueOf(position)).then().statusCode(403);
        assertEquals(chapterCount, Chapter.count("post", post));
        assertEquals(1, Chapter.count("id", chapterId));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @TestSecurity(user = "user_1")
    public void deleteChapterInOtherDraft(final int position) {
        final var chapterCount = Chapter.count("post", draft);
        final var chapterId = draft.getChapters().get(position).id;
        given().pathParam("id", draft.id)
                .delete(String.valueOf(position))
                .then()
                .statusCode(404);
        assertEquals(chapterCount, Chapter.count("post", draft));
        assertEquals(1, Chapter.count("id", chapterId));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    public void deleteChapterInPostWhileUnauthenticated(final int position) {
        final var chapterCount = Chapter.count("post", post);
        final var chapterId = post.getChapters().get(position).id;
        given().pathParam("id", post.id).delete(String.valueOf(position)).then().statusCode(401);
        assertEquals(chapterCount, Chapter.count("post", post));
        assertEquals(1, Chapter.count("id", chapterId));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    public void deleteChapterInDraftWhileUnauthenticated(final int position) {
        final var chapterCount = Chapter.count("post", draft);
        final var chapterId = draft.getChapters().get(position).id;
        given().pathParam("id", draft.id)
                .delete(String.valueOf(position))
                .then()
                .statusCode(401);
        assertEquals(chapterCount, Chapter.count("post", draft));
        assertEquals(1, Chapter.count("id", chapterId));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2})
    @TestSecurity(user = "user_0")
    public void deleteChapterInNonExistentPost(final int position) {
        final var chapterCount = Chapter.count();
        given().pathParam("id", fakeId).delete(String.valueOf(position)).then().statusCode(404);
        assertEquals(chapterCount, Chapter.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {"fake", "null"})
    @TestSecurity(user = "user_0")
    @Transactional
    public void deleteNonExistentChapter(final String position) {
        given().pathParam("id", draft.id)
                .delete(String.valueOf(position))
                .then()
                .statusCode(404);
    }
}
