package app.fyreplace.api.data;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record VoteCreation(@Schema(required = true) boolean spread) {}
