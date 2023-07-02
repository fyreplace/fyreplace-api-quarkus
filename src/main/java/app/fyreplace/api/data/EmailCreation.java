package app.fyreplace.api.data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

public final record EmailCreation(@NotNull @Length(min = 3, max = 254) @NotNull @Email String email) {}
