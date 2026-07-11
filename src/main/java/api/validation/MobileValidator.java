package api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class MobileValidator implements ConstraintValidator<ValidMobile, String> {

    @Override
    public boolean isValid(String mobile, ConstraintValidatorContext context) {

        if (mobile == null || mobile.isBlank()) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        if (!mobile.matches("\\d+")) {
            context.buildConstraintViolationWithTemplate("Mobile number must contain only digits")
                    .addConstraintViolation();
            return false;
        }

        if (mobile.length() != 10) {
            context.buildConstraintViolationWithTemplate("Mobile number must be exactly 10 digits")
                    .addConstraintViolation();
            return false;
        }

        if (!mobile.startsWith("9")) {
            context.buildConstraintViolationWithTemplate("Mobile number must start with 9")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
