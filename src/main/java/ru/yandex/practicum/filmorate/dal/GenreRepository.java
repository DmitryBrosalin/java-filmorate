package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class GenreRepository extends BaseRepository<Genre> {
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE genre_id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM genres";
    private static final String FIND_GENRES_QUERY = "SELECT * FROM genres WHERE genre_id IN " +
            "(SELECT genre_id FROM film_genres WHERE film_id = ?)";

    public GenreRepository(JdbcTemplate jdbc, GenreRowMapper mapper) {
        super(jdbc, mapper);
    }

    public Genre findById(int id) {
        Optional<Genre> genreOpt = findOne(FIND_BY_ID_QUERY, id);
        if (genreOpt.isPresent()) {
            return genreOpt.get();
        } else {
            throw new NotFoundException("Жанр с id = " + id + " не найден.");
        }
    }

    public List<Genre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Set<Genre> findGenresById(long filmId) {
        return Set.copyOf(findMany(FIND_GENRES_QUERY, filmId));
    }
}
