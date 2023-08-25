package app.fyreplace.api.data;

import jakarta.validation.constraints.NotBlank;

public record EmailActivation(@NotBlank String email, @NotBlank String code) {}
