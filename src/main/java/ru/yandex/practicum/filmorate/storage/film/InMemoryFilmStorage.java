package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films;
    private final TreeSet popularFilms;

    public InMemoryFilmStorage() {
        this.films = new HashMap<>();
        this.popularFilms = new TreeSet<>();
    }

    @Override
    public Collection<Film> getFilms() {
        return films.values();
    }

    @Override
    public Collection<Film> getPopularFilms(long size) {
        return films.values().stream()
                .sorted(Comparator.reverseOrder())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public Optional<Film> getFilm(long id) {
        if (films.containsKey(id)) {
            return Optional.of(films.get(id));
        } else {
            throw new NotFoundException("Фильм с id = " + id + " не найден.");
        }
    }

    @Override
    public Film createFilm(Film film) {
        film.setId(getNextId());
        film.setLikes(new HashSet<>());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден.");
        }
        if (film.getLikes() == null) {
            film.setLikes(new HashSet<>());
        }
        films.replace(film.getId(), film);
        return film;
    }

    @Override
    public Long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
