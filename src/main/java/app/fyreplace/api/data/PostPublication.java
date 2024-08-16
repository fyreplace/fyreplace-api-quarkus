package app.fyreplace.api.data;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record PostPublication(@Schema(required = true) boolean anonymous) {}
