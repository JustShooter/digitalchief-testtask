package by.iyunski.library.service.annotations;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.Year;

import static java.util.Objects.isNull;

public class YearValidator implements ConstraintValidator<YearValidation, Integer> {

    public boolean isValid(Integer year, ConstraintValidatorContext context) {
        if (isNull(year)) {
            return true;
        } else if (year < 1) {
            return false;
        } else if (Year.now().isAfter(Year.of(year))) {
            return true;
        } else {
            return Year.now().equals(Year.of(year));
        }
    }
}
