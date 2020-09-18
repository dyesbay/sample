package app.expert.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class GPhoneValidator implements ConstraintValidator<GPhone, String> {
    @Override
    public void initialize(GPhone constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return false;
        value = GPhoneParser.parsePhone(value);
        return value.matches("\\+?7?[0-9]+");
    }
}
