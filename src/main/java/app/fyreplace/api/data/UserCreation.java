package app.fyreplace.api.data;

import app.fyreplace.api.data.validators.Regex;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record UserCreation(
        @NotBlank @Length(min = 3, max = 254) @Email String email,
        @NotBlank @Length(min = 3, max = 50) @Regex(pattern = "^[\\w.@+-]+\\Z") String username) {}
