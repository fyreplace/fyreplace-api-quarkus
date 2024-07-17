package app.fyreplace.api.exceptions.mappers;

import app.fyreplace.api.exceptions.ForbiddenException;
import jakarta.ws.rs.ext.Provider;

@SuppressWarnings("unused")
@Provider
public final class ForbiddenExceptionMapper extends ExplainableExceptionMapper<ForbiddenException> {}
