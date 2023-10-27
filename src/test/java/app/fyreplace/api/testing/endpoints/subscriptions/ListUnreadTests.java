package app.fyreplace.api.testing.endpoints.subscriptions;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

import app.fyreplace.api.endpoints.SubscriptionsEndpoint;
import app.fyreplace.api.testing.SubscriptionTestsBase;
import io.quarkus.test.common.http.TestHTTPEndpoint;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

@QuarkusTest
@TestHTTPEndpoint(SubscriptionsEndpoint.class)
public final class ListUnreadTests extends SubscriptionTestsBase {
    @ConfigProperty(name = "app.paging.size")
    int pagingSize;

    @Test
    @TestSecurity(user = "user_0")
    public void listUnread() {
        given().get("unread")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", equalTo(pagingSize))
                .body("[0].post.id", equalTo(post.id.toString()))
                .body("[0].unreadCommentCount", equalTo(2))
                .body("[1].unreadCommentCount", equalTo(1));
    }

    @Override
    public int getPostCount() {
        return 20;
    }
}
