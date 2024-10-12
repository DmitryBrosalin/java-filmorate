package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DescriptionValidator implements ConstraintValidator<DescriptionConstraint, String> {
    @Override
    public void initialize(DescriptionConstraint descriptionConstraint) {
    }

    @Override
    public boolean isValid(String descriptionField,
                           ConstraintValidatorContext cxt) {
        return (descriptionField.length() <= 200);
    }
}
