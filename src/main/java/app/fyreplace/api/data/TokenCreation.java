package app.fyreplace.api.data;

import jakarta.validation.constraints.NotBlank;

public record TokenCreation(@NotBlank String identifier, @NotBlank String code) {}
