package app.fyreplace.api.exceptions;

import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

@SuppressWarnings("unused")
public final class ExceptionMappers {
    @ServerExceptionMapper
    public Response handleForbiddenException(final ForbiddenException exception) {
        return Responses.makeFrom(exception);
    }

    @ServerExceptionMapper
    public Response handleConflictException(final ConflictException exception) {
        return Responses.makeFrom(exception);
    }

    @ServerExceptionMapper
    public Response handleUnsupportedMediaTypeException(final UnsupportedMediaTypeException exception) {
        return Responses.makeFrom(exception);
    }

    @ServerExceptionMapper
    public Response handleNumberFormatException(final NumberFormatException exception) {
        return Response.status(Response.Status.BAD_REQUEST).build();
    }
}
