package app.fyreplace.api.data;

import jakarta.validation.constraints.NotNull;

public final record NewTokenCreation(@NotNull String identifier) {}
