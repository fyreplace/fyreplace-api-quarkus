package app.fyreplace.api.exceptions.mappers;

import app.fyreplace.api.exceptions.ExplainableException;
import app.fyreplace.api.exceptions.Responses;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

public abstract class ExplainableExceptionMapper<E extends WebApplicationException & ExplainableException>
        implements ExceptionMapper<E> {
    @Override
    public Response toResponse(final E exception) {
        return Responses.makeFrom(exception);
    }
}
