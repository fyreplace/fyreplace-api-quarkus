package app.fyreplace.api.data;

import app.fyreplace.api.data.validators.Length;
import jakarta.validation.constraints.NotBlank;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public record CommentCreation(
        @Length(min = 1, max = Comment.TEXT_MAX_LENGTH)
                @NotBlank
                @Schema(minLength = 1, maxLength = Comment.TEXT_MAX_LENGTH)
                String text,
        boolean anonymous) {}
