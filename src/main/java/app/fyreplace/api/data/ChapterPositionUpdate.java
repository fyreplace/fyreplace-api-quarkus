package app.fyreplace.api.data;

import jakarta.validation.constraints.PositiveOrZero;

public record ChapterPositionUpdate(@PositiveOrZero int position) {}
