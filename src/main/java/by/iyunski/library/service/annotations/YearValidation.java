package by.iyunski.library.service.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Constraint(validatedBy = {YearValidator.class})
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface YearValidation {
    String message() default "must be a past date and be in range from the first to the current year";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
