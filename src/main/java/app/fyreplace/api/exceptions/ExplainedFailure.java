package app.fyreplace.api.exceptions;

public record ExplainedFailure(String title, int status, String reason) {}
