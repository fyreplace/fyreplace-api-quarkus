package app.fyreplace.api.exceptions;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

public final class GoneException extends ClientErrorException {
    public GoneException() {
        super(Response.Status.GONE);
    }
}
