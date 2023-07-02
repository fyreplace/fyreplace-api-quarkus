package app.fyreplace.api.data;

import jakarta.validation.constraints.NotNull;

public record TokenCreation(@NotNull String identifier, @NotNull String code) {}
