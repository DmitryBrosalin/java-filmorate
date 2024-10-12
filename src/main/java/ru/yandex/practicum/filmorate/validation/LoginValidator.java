package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class LoginValidator implements ConstraintValidator<LoginConstraint, String> {
    @Override
    public void initialize(LoginConstraint loginConstraint) {

    }

    @Override
    public boolean isValid(String login,
                           ConstraintValidatorContext cxt) {
        return !login.contains(" ");
    }
}
