package app.fyreplace.api.data;

import jakarta.validation.constraints.Min;

public record ChapterPositionUpdate(@Min(0) int position) {}
