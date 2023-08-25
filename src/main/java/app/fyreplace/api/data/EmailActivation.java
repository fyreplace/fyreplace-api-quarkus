package app.fyreplace.api.data;

import jakarta.validation.constraints.NotBlank;

public final record EmailActivation(@NotBlank String email, @NotBlank String code) {}
