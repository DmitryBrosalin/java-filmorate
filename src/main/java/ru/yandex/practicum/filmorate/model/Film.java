package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.DescriptionConstraint;
import ru.yandex.practicum.filmorate.validation.ReleaseDateConstraint;

import java.time.LocalDate;
import java.util.Set;

@Data
public class Film implements Comparable<Film> {
    private long id;
    @NotBlank
    private String name;
    @DescriptionConstraint
    private String description;
    @ReleaseDateConstraint
    LocalDate releaseDate;
    @Positive
    int duration;
    Set<Long> likes;

    @Override
    public int compareTo(Film film) {
        return this.getLikes().size() - film.getLikes().size();
    }
}
