package app.fyreplace.api.exceptions;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

public final class ConflictException extends ClientErrorException implements ExplainableException {
    private final String explanationValue;

    public ConflictException(final String explanationValue) {
        super(Response.Status.CONFLICT);
        this.explanationValue = explanationValue;
    }

    @Override
    public Object getExplanationValue() {
        return explanationValue;
    }
}
