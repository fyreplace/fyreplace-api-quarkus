package app.fyreplace.api.exceptions;

public final class ForbiddenException extends jakarta.ws.rs.ForbiddenException implements ExplainableException {
    private final String explanation;

    public ForbiddenException(final String explanation) {
        super();
        this.explanation = explanation;
    }

    @Override
    public String getExplanationValue() {
        return explanation;
    }
}
