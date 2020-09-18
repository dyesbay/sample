package app.expert.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = GPhoneValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GPhone {
    String message() default "Невалидный номер телефона";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
