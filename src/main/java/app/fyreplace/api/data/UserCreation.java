package app.fyreplace.api.data;

import app.fyreplace.api.data.validators.Regex;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public record UserCreation(
        @NotNull @Length(min = 3, max = 254) @Email String email,
        @NotNull @Length(min = 3, max = 100) @Regex(pattern = "^[\\w.@+-]+\\Z") String username) {}
