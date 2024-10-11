package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Film {
    private long id;
    @NotBlank
    private String name;
    private String description;
    LocalDate releaseDate;
    @Positive
    int duration;
}
