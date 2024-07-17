package app.fyreplace.api.data;

import jakarta.validation.constraints.NotBlank;

public record NewTokenCreation(@NotBlank String identifier) {}
