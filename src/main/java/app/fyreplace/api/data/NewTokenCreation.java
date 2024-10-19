package app.fyreplace.api.data;

import app.fyreplace.api.data.validators.Length;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record NewTokenCreation(
        @NotBlank @Length(max = Email.EMAIL_MAX_LENGTH) @Schema(maxLength = Email.EMAIL_MAX_LENGTH)
                String identifier) {}
