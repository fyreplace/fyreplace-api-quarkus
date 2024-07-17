package app.fyreplace.api.exceptions;

import io.vertx.core.json.JsonObject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public final class Responses {
    public static <E extends WebApplicationException & ExplainableException> Response makeFrom(final E exception) {
        final var response = exception.getResponse();
        final var body = new JsonObject()
                .put("title", response.getStatusInfo().getReasonPhrase())
                .put("status", response.getStatus())
                .put("reason", exception.getExplanationValue());
        return Response.status(response.getStatus()).entity(body).build();
    }
}
