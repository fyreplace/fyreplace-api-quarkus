package app.fyreplace.api.data.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public final class LengthValidator implements ConstraintValidator<Length, String> {
    private int min;
    private int max;

    @Override
    public void initialize(final Length constraintAnnotation) {
        min = constraintAnnotation.min();
        max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        final var count = value.codePointCount(0, value.length());
        return count >= min && count <= max;
    }
}
