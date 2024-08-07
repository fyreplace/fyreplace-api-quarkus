package app.fyreplace.api.data;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.validator.constraints.Length;

public record EmailCreation(
        @NotBlank
                @Length(min = Email.EMAIL_MIN_LENGTH, max = Email.EMAIL_MAX_LENGTH)
                @jakarta.validation.constraints.Email
                @Schema(minLength = Email.EMAIL_MIN_LENGTH, maxLength = Email.EMAIL_MAX_LENGTH)
                String email) {}
