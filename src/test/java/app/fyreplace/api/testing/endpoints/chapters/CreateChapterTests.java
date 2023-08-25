package app.fyreplace.api.testing.endpoints.chapters;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import app.fyreplace.api.data.Chapter;
import app.fyreplace.api.endpoints.ChaptersEndpoint;
import app.fyreplace.api.testing.endpoints.PostTestsBase;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.panache.common.Sort;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(ChaptersEndpoint.class)
public final class CreateChapterTests extends PostTestsBase {
    @ConfigProperty(name = "app.posts.max-chapter-count")
    int postsMaxChapterCount;

    @Test
    @TestSecurity(user = "user_0")
    public void createChapterInOwnPost() {
        final var chapterCount = post.getChapters().size();
        given().pathParam("id", post.id).post().then().statusCode(403);
        assertEquals(chapterCount, Chapter.count("post", post));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void createChapterInOwnDraft() {
        final var chapterCount = draft.getChapters().size();
        final var response = given().pathParam("id", draft.id)
                .post()
                .then()
                .statusCode(201)
                .body("id", isA(String.class))
                .body("text", equalTo(""))
                .body("image", nullValue())
                .body("width", equalTo(0))
                .body("height", equalTo(0));
        assertEquals(chapterCount + 1, Chapter.count("post", draft));
        final var lastChapter = Chapter.<Chapter>find("post", Sort.descending("position"), draft)
                .firstResult();
        response.body("id", equalTo(lastChapter.id.toString()));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void createChapterInOwnDraftOverMaximum() {
        QuarkusTransaction.requiringNew().run(() -> {
            final var chapters = draft.getChapters();
            final var newChapters = postsMaxChapterCount - chapters.size();
            String before = chapters.get(chapters.size() - 1).position;

            for (var i = 0; i < newChapters; i++) {
                final var chapter = new Chapter();
                chapter.post = draft;
                chapter.position = Chapter.positionBetween(before, null);
                chapter.persist();
                before = chapter.position;
            }
        });

        final var chapterCount = draft.getChapters().size();
        given().pathParam("id", draft.id).post().then().statusCode(403);
        assertEquals(chapterCount, Chapter.count("post", draft));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void createChapterInOtherPost() {
        final var chapterCount = post.getChapters().size();
        given().pathParam("id", post.id).post().then().statusCode(403);
        assertEquals(chapterCount, Chapter.count("post", post));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void createChapterInOtherDraft() {
        final var chapterCount = draft.getChapters().size();
        given().pathParam("id", draft.id).post().then().statusCode(404);
        assertEquals(chapterCount, Chapter.count("post", draft));
    }

    @Test
    public void createChapterInPostUnauthenticated() {
        final var chapterCount = post.getChapters().size();
        given().pathParam("id", post.id).post().then().statusCode(401);
        assertEquals(chapterCount, Chapter.count("post", post));
    }

    @Test
    public void createChapterInDraftUnauthenticated() {
        final var chapterCount = draft.getChapters().size();
        given().pathParam("id", draft.id).post().then().statusCode(401);
        assertEquals(chapterCount, Chapter.count("post", draft));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void createChapterInNonExistentPost() {
        final var chapterCount = Chapter.count();
        given().pathParam("id", fakeId).post().then().statusCode(404);
        assertEquals(chapterCount, Chapter.count());
    }
}
