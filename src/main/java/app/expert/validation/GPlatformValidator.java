package app.expert.validation;

import app.expert.constants.ManagerPlatform;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class GPlatformValidator implements ConstraintValidator<GPlatform, String> {
    @Override
    public void initialize(GPlatform constraintAnnotation) {
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // потому что поле необязательное
        if (value == null) return true;

       return ManagerPlatform.contains(value);
    }
}
