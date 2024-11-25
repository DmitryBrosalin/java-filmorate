package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FilmDirector {
    @NotNull
    private long filmId;

    @NotNull
    private long directorId;
}
