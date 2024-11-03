package app.fyreplace.api.data.validators;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@SuppressWarnings("unused")
@Retention(RUNTIME)
@Target({FIELD, PARAMETER})
@Constraint(validatedBy = LengthValidator.class)
public @interface Length {
    String message() default "does not meet length requirements";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    int min() default 0;

    int max() default Integer.MAX_VALUE;
}
