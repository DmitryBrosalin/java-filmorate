package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> getFilms() {
        return filmService.getFilms();
    }

    @GetMapping(value = "/popular")
    public Collection<Film> getPopularFilms(@RequestParam(defaultValue = "10") long count,
                                            @RequestParam(required = false) Integer genreId,
                                            @RequestParam(required = false) Integer year) {
        return filmService.getPopularFilms(count, genreId, year);
    }

    @GetMapping(value = "/{id}")
    public Film findFilm(@PathVariable long id) {
        return filmService.getFilm(id);
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        return filmService.createFilm(film);
    }

    @PutMapping
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.updateFilm(film);
    }

    @PutMapping(value = "/{id}/like/{userId}")
    public void addLike(@PathVariable long id,
                        @PathVariable long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping(value = "/{id}/like/{userId}")
    public void deleteLike(@PathVariable long id,
                        @PathVariable long userId) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping(value = "/common")
    public Collection<Film> getCommonFilms(@RequestParam long userId,
                                           @RequestParam long friendId) {
        return filmService.getCommonFilms(userId, friendId);
    }

    @DeleteMapping(value = "/{id}")
    public void deleteLike(@PathVariable long id) {
        filmService.deleteFilm(id);
    }
}
