package app.fyreplace.api.exceptions.mappers;

import app.fyreplace.api.exceptions.UnsupportedMediaTypeException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@SuppressWarnings("unused")
@Provider
public final class UnsupportedMediaTypeExceptionMapper implements ExceptionMapper<UnsupportedMediaTypeException> {
    @Override
    public Response toResponse(final UnsupportedMediaTypeException exception) {
        return Response.status(Response.Status.UNSUPPORTED_MEDIA_TYPE).build();
    }
}
