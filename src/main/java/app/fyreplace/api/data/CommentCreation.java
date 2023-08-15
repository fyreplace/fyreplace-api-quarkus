package app.fyreplace.api.data;

import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.Length;

public record CommentCreation(@Length(min = 1, max = 1500) @NotBlank String text, boolean anonymous) {}
