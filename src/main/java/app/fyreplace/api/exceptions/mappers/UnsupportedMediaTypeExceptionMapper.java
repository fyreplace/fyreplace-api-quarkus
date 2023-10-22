package app.fyreplace.api.exceptions.mappers;

import app.fyreplace.api.exceptions.UnsupportedMediaTypeException;
import jakarta.ws.rs.ext.Provider;

@SuppressWarnings("unused")
@Provider
public final class UnsupportedMediaTypeExceptionMapper
        extends ExplainableExceptionMapper<UnsupportedMediaTypeException> {}
