package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> getFilms() {
        return films.values();
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Release Date must not be before 28/12/1895");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Description length must be 200 or less");
        }
        film.setId(getNextId());
        log.debug("Set id to film.");
        films.put(film.getId(), film);
        log.debug("Put film to Map.");
        return film;
    }

    @PutMapping
    public Film upfateFilm(@Valid @RequestBody Film film) {
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Film id not found");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Release Date must not be before 28/12/1895");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Description length must be 200 or less");
        }
        films.replace(film.getId(), film);
        log.debug("Updated film in Map.");
        return film;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
