package api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = MobileValidator.class)
@Documented
public @interface ValidMobile {

    String message() default "Mobile number must start with 9 and be exactly 10 digits long";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
