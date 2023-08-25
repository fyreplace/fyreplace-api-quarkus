package app.fyreplace.api.data;

import jakarta.validation.constraints.NotBlank;

public final record NewTokenCreation(@NotBlank String identifier) {}
