package app.fyreplace.api.exceptions;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

public class RequestEntityTooLargeException extends ClientErrorException {
    public RequestEntityTooLargeException() {
        super(Response.Status.REQUEST_ENTITY_TOO_LARGE);
    }
}
