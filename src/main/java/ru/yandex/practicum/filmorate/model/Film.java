package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.DescriptionConstraint;
import ru.yandex.practicum.filmorate.validation.ReleaseDateConstraint;

import java.time.LocalDate;

@Data
public class Film {
    private long id;
    @NotBlank
    private String name;
    @DescriptionConstraint
    private String description;
    @ReleaseDateConstraint
    LocalDate releaseDate;
    @Positive
    int duration;
}
