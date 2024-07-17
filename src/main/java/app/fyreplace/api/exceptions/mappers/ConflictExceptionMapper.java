package app.fyreplace.api.exceptions.mappers;

import app.fyreplace.api.exceptions.ConflictException;
import jakarta.ws.rs.ext.Provider;

@SuppressWarnings("unused")
@Provider
public final class ConflictExceptionMapper extends ExplainableExceptionMapper<ConflictException> {}
