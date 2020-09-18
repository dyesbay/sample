package app.expert.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = GPlatformValidator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface GPlatform {
    String message() default "Введенная платформа не предусмотрен";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
