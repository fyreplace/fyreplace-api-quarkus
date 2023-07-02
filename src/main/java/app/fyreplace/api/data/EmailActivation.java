package app.fyreplace.api.data;

import jakarta.validation.constraints.NotNull;

public final record EmailActivation(@NotNull String email, @NotNull String code) {}
