package app.fyreplace.api.data.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public final class RegexValidator implements ConstraintValidator<Regex, String> {
    private Pattern pattern;

    @Override
    public void initialize(final Regex constraintAnnotation) {
        pattern = Pattern.compile(constraintAnnotation.pattern());
    }

    @Override
    public boolean isValid(final String value, final ConstraintValidatorContext context) {
        return pattern.matcher(value).matches();
    }
}
