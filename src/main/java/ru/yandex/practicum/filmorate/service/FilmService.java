package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.DirectorRepository;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

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
        if (directorRepository.findById(directorId) != null) {
            if (!"year".equals(sortBy) && !"likes".equals(sortBy)) {
                throw new BadRequestException("Ошибка запроса");
            }
        }
        return filmRepository.getFilmsByDirector(directorId, sortBy);
    }

    public List<Film> findFilm(Optional<String> query, Optional<List<String>> sortBy) {
        if (query.isEmpty() && sortBy.isEmpty()) {
            return filmRepository.getPopularFilms(10, null, null).stream().toList();
        } else if (query.isPresent() && sortBy.isPresent()) {
            List<String> sortByList = sortBy.get();
            if (sortByList.size() == 1) {
                if (sortByList.getFirst().equalsIgnoreCase("title")) {
                    return filmRepository.findFilms(queryBuilder(sortByList.getFirst(), query.get()));
                } else if (sortByList.getFirst().equalsIgnoreCase("director")) {
                    return getFilmsFromDirectors(sortByList.getFirst(), query.get());
                } else throw new ValidationException("Ошибка в параметре " + sortByList.getFirst());
            } else if (sortByList.size() == 2) {
                List<Film> filmsQuery = filmRepository.findFilms(queryBuilder(sortByList.getFirst(), query.get()));
                List<Film> dirQuery = getFilmsFromDirectors(sortByList.getLast(), query.get());
                dirQuery.addAll(filmsQuery);
                return dirQuery;
            } else return Collections.emptyList();
        } else {
            throw new BadRequestException("Ошибка в запросе");
        }
    }

    private String queryBuilder(String sortParameter, String query) {
        if (sortParameter.equalsIgnoreCase("title")) {
            return "SELECT * FROM films WHERE LOWER(title) LIKE LOWER(" + "'%" + query.toLowerCase() + "%')";
        } else {
            return "SELECT * FROM directors WHERE LOWER(director_name) LIKE LOWER(" + "'%" + query.toLowerCase() + "%')";
        }
    }

    private List<Film> getFilmsFromDirectors(String sortByList, String query) {
        List<Director> directorList = directorRepository.findDirectors(queryBuilder(sortByList, query));
        if (!directorList.isEmpty()) {
            return (List<Film>) filmRepository.getAllFilmsByDirectors(directorList);
        }
        return Collections.emptyList();
    }
}
