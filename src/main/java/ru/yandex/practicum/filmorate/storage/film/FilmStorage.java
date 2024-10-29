package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> getFilms();

    Collection<Film> getPopularFilms(long size);

    Optional<Film> getFilm(long id);

    Film createFilm(Film film);

    Film updateFilm(Film film);

    Long getNextId();
}
