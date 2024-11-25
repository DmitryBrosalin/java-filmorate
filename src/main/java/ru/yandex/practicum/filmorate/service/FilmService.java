package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

@Service
public class FilmService {
    private final FilmRepository filmRepository;
    private final UserRepository userRepository;
    private final DirectorRepository directorRepository;
    private final DirectorService directorService;

    public FilmService(FilmRepository filmRepository, UserRepository userRepository, DirectorRepository directorRepository, DirectorService directorService) {
        this.filmRepository = filmRepository;
        this.userRepository = userRepository;
        this.directorRepository = directorRepository;
        this.directorService = directorService;
    }

    public void addLike(long filmId, long userId) {
        if (filmRepository.findById(filmId) != null && userRepository.findById(userId) != null) {
            filmRepository.addLike(filmId, userId);
        }
    }

    public void deleteLike(long filmId, long userId) {
        if (filmRepository.findById(filmId) != null && userRepository.findById(userId) != null) {
            filmRepository.deleteLike(filmId, userId);
        }
    }

    public Collection<Film> getFilms() {
        return filmRepository.findAll();
    }

    public Collection<Film> getPopularFilms(long size, Integer genreId, Integer year) {
        return filmRepository.getPopularFilms(size, genreId, year);
    }

    public Film getFilm(long id) {
        return filmRepository.findById(id);
    }

    public Film createFilm(Film film) {
        return filmRepository.save(film);
    }

    public Film updateFilm(Film film) {
        return filmRepository.update(film);
    }

    public void deleteFilm(long filmId) {
        filmRepository.deleteFilm(filmId);
    }

    public Collection<Film> getCommonFilms(long userId, long friendId) {
        return filmRepository.getCommonFilms(userId, friendId);
    }

    public Collection<Film> getFilmsByDirector(long directorId, String sortBy) {
        if (!"year".equals(sortBy) && !"likes".equals(sortBy)) {
            throw new NotFoundException("Ошибка запроса");
        }
        return filmRepository.getFilmsByDirector(directorId, sortBy);
    }
}
