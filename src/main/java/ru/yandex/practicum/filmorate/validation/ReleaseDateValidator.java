package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

public class ReleaseDateValidator implements ConstraintValidator<ReleaseDateConstraint, LocalDate> {
    @Override
    public void initialize(ReleaseDateConstraint releaseDateConstraint){
    }

    @Override
    public boolean isValid(LocalDate releaseDate,
                           ConstraintValidatorContext cxt) {
        return !releaseDate.isBefore(LocalDate.of(1895, 12, 28));
    }
}
