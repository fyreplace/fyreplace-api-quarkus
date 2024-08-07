package app.fyreplace.api.data;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record ReportUpdate(@Schema(required = true) boolean reported) {}
