package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;

@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserService userService;

    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public void addLike(long filmId, long userId) {
        if (filmStorage.getFilm(filmId).isPresent() && userService.findUser(userId).isPresent()) {
            getFilm(filmId).getLikes().add(userId);
        }
    }

    public void deleteLike(long filmId, long userId) {
        if (filmStorage.getFilm(filmId).isPresent() && userService.findUser(userId).isPresent()) {
            getFilm(filmId).getLikes().remove(userId);
        }
    }

    public Collection<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public Collection<Film> getPopularFilms(long size) {
        return filmStorage.getPopularFilms(size);
    }

    public Film getFilm(long id) {
        return filmStorage.getFilm(id).get();
    }

    public Film createFilm(Film film) {
        return filmStorage.createFilm(film);
    }

    public Film updateFilm(Film film) {
        return filmStorage.updateFilm(film);
    }
}
