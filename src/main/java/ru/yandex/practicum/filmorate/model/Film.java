package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.DescriptionConstraint;
import ru.yandex.practicum.filmorate.validation.ReleaseDateConstraint;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

@Data
public class Film implements Comparable<Film>, Serializable {
    private long id;
    @NotBlank
    private String name;
    @DescriptionConstraint
    private String description;
    @ReleaseDateConstraint
    private LocalDate releaseDate;
    @Positive
    private int duration;
    @NotNull
    private Mpa mpa;
    private Set<Genre> genres = new TreeSet<>();
    private Set<Long> likes = new TreeSet<>();
    private Set<Director> directors = new LinkedHashSet<>();

    @Override
    public int compareTo(Film film) {
        return this.getLikes().size() - film.getLikes().size();
    }
}
