package app.fyreplace.api.data;

import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.hibernate.validator.constraints.Length;

public record CommentCreation(
        @Length(min = 1, max = Comment.TEXT_MAX_LENGTH)
                @NotBlank
                @Schema(minLength = 1, maxLength = Comment.TEXT_MAX_LENGTH)
                String text,
        boolean anonymous) {}
