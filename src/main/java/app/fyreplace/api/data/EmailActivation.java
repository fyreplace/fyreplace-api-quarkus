package app.fyreplace.api.data;

import app.fyreplace.api.data.validators.Length;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record EmailActivation(
        @NotBlank
                @Length(min = Email.EMAIL_MIN_LENGTH, max = Email.EMAIL_MAX_LENGTH)
                @jakarta.validation.constraints.Email
                @Schema(minLength = Email.EMAIL_MIN_LENGTH, maxLength = Email.EMAIL_MAX_LENGTH)
                String email,
        @NotBlank String code) {}
