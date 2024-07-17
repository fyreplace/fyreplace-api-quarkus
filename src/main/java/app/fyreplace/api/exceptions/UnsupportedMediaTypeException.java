package app.fyreplace.api.exceptions;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

public final class UnsupportedMediaTypeException extends ClientErrorException {
    public UnsupportedMediaTypeException() {
        super(Response.Status.UNSUPPORTED_MEDIA_TYPE);
    }
}
