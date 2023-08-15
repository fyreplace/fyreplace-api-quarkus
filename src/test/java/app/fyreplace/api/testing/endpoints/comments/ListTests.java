package app.fyreplace.api.testing.endpoints.comments;

import static io.restassured.RestAssured.given;
import static java.util.stream.IntStream.range;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.in;
import static org.hamcrest.Matchers.nullValue;

import app.fyreplace.api.data.Comment;
import app.fyreplace.api.data.User;
import app.fyreplace.api.data.dev.DataSeeder;
import app.fyreplace.api.endpoints.CommentsEndpoint;
import app.fyreplace.api.testing.endpoints.PostTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(CommentsEndpoint.class)
public class ListTests extends PostTestsBase {
    @ConfigProperty(name = "app.paging.size")
    int pagingSize;

    @Inject
    DataSeeder dataSeeder;

    private final List<String> commentIds = new ArrayList<>();

    @Test
    @TestSecurity(user = "user_0")
    public void listInOwnPost() {
        final var response = given().pathParam("id", post.id)
                .queryParam("page", 0)
                .get()
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(pagingSize))
                .body("[0].id", in(commentIds))
                .body("[0].author.username", equalTo("user_0"))
                .body("[0].anonymous", equalTo(true));

        range(1, pagingSize).forEach(i -> response.body("[" + i + "].id", in(commentIds))
                .body("[" + i + "].author.username", equalTo("user_1")));
    }

    @Test
    @TestSecurity(user = "user_1")
    public void listInOtherPost() {
        final var response = given().pathParam("id", post.id)
                .queryParam("page", 0)
                .get()
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(pagingSize))
                .body("[0].id", in(commentIds))
                .body("[0].author", nullValue())
                .body("[0].anonymous", equalTo(true));

        range(1, pagingSize).forEach(i -> response.body("[" + i + "].id", in(commentIds))
                .body("[" + i + "].author.username", equalTo("user_1")));
    }

    @Test
    @TestSecurity(user = "user_0")
    public void listOutOfBounds() {
        final var page = -1;
        given().pathParam("id", post.id).queryParam("page", page).get().then().statusCode(400);
    }

    @Test
    @TestSecurity(user = "user_0")
    public void listTooFar() {
        final var page = 50;
        given().pathParam("id", post.id)
                .queryParam("page", page)
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
        Comment.deleteAll();
        final var user0 = User.findByUsername("user_0");
        final var user1 = User.findByUsername("user_1");
        commentIds.add(
                dataSeeder.createComment(user0, post, "Comment 0", true).id.toString());
        range(1, pagingSize)
                .forEach(i -> commentIds.add(dataSeeder
                        .createComment(user1, post, "Comment " + i, false)
                        .id
                        .toString()));
    }
}
