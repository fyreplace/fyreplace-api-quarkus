package app.fyreplace.api.exceptions;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

public final class Responses {
    public static <E extends WebApplicationException & ExplainableException> Response makeFrom(final E exception) {
        final var response = exception.getResponse();
        final var failure = new ExplainedFailure(
                response.getStatusInfo().getReasonPhrase(), response.getStatus(), exception.getExplanationValue());
        return Response.status(response.getStatus()).entity(failure).build();
    }
}
