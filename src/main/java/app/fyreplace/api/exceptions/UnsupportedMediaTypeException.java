package app.fyreplace.api.exceptions;

import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

public final class UnsupportedMediaTypeException extends ClientErrorException implements ExplainableException {
    private final String explanationValue;

    public UnsupportedMediaTypeException(final String explanationValue) {
        super(Response.Status.UNSUPPORTED_MEDIA_TYPE);
        this.explanationValue = explanationValue;
    }

    @Override
    public Object getExplanationValue() {
        return explanationValue;
    }
}
