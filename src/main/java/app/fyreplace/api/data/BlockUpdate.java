package app.fyreplace.api.data;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record BlockUpdate(@Schema(required = true) boolean blocked) {}
