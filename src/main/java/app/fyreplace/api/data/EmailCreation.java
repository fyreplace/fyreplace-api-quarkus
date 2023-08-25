package app.fyreplace.api.data;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public final record EmailCreation(@Length(min = 3, max = 254) @NotBlank @Email String email) {}
