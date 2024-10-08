package app.fyreplace.api.data;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.validator.constraints.Length;

public record TokenCreation(
        @NotBlank @Length(max = Email.EMAIL_MAX_LENGTH) @Schema(maxLength = Email.EMAIL_MAX_LENGTH) String identifier,
        @NotBlank @Schema(format = "password") String secret) {}
