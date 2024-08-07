package app.fyreplace.api.data;

import app.fyreplace.api.data.validators.Regex;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.validator.constraints.Length;

public record UserCreation(
        @NotBlank
                @Length(min = Email.EMAIL_MIN_LENGTH, max = Email.EMAIL_MAX_LENGTH)
                @jakarta.validation.constraints.Email
                @Schema(minLength = Email.EMAIL_MIN_LENGTH, maxLength = Email.EMAIL_MAX_LENGTH)
                String email,
        @NotBlank
                @Length(min = User.USERNAME_MIN_LENGTH, max = User.USERNAME_MAX_LENGTH)
                @Regex(pattern = User.USERNAME_PATTERN)
                @Schema(
                        minLength = User.USERNAME_MIN_LENGTH,
                        maxLength = User.USERNAME_MAX_LENGTH,
                        pattern = User.USERNAME_PATTERN)
                String username) {}
