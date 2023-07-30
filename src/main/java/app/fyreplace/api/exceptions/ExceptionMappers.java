package app.fyreplace.api.exceptions;

import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

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
}