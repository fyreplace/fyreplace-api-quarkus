package app.fyreplace.api.data;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record Color(
        @Schema(required = true, minimum = "0", maximum = "255", format = "uint8") int r,
        @Schema(required = true, minimum = "0", maximum = "255", format = "uint8") int g,
        @Schema(required = true, minimum = "0", maximum = "255", format = "uint8") int b) {}
