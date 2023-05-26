package app.fyreplace

import jakarta.ws.rs.GET
import jakarta.ws.rs.Path

@Path("/health")
@Suppress("unused")
class HealthResource {
    @GET
    fun list() = Unit
}
