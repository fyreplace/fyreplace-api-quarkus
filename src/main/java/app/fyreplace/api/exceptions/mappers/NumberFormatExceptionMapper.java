package app.fyreplace.api.exceptions.mappers;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@SuppressWarnings("unused")
@Provider
public final class NumberFormatExceptionMapper implements ExceptionMapper<NumberFormatException> {
    @Override
    public Response toResponse(final NumberFormatException exception) {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }
}
