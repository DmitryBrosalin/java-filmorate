package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.GenreRepository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@Service
public class GenreService {
    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    public Collection<Genre> getGenres() {
        return genreRepository.findAll();
    }

    public Genre getGenre(int genreId) {
        return genreRepository.findById(genreId);
    }
}
