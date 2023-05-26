package app.fyreplace

import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.given
import org.junit.jupiter.api.Test

@QuarkusTest
class HealthResourceTest {
    @Test
    fun testListEndpoint() {
        given()
            .`when`().get("/health")
            .then()
            .statusCode(204)
    }
}
