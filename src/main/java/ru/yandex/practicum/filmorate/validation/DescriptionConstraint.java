package ru.yandex.practicum.filmorate.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = DescriptionValidator.class)
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DescriptionConstraint {
    String message() default "Invalid description: description length must be 200 or less";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
