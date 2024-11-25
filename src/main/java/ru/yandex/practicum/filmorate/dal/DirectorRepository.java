package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.DirectorRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class DirectorRepository extends BaseRepository<Director> {

    public DirectorRepository(JdbcTemplate jdbc, DirectorRowMapper mapper) {
        super(jdbc, mapper);
    }

    private static final String FIND_ALL_QUERY =
            "SELECT * FROM directors;";

    private static final String FIND_BY_ID_QUERY =
            "SELECT * FROM directors WHERE director_id = ?;";
    private static final String FIND_DIRECTORS_BY_FILM_ID_QUERY =
            "SELECT * FROM directors WHERE director_id IN \n" +
                    "(SELECT fd.director_id FROM film_directors AS fd WHERE fd.film_id = ?);";

    private static final String INSERT_QUERY =
            "INSERT INTO directors (director_name) " +
                    "VALUES (?);";

    private static final String DELETE_QUERY =
            "DELETE FROM directors WHERE director_id = ?";

    private static final String UPDATE_QUERY =
            "UPDATE directors SET director_name = ? WHERE director_id = ?;";


    public List<Director> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Director findById(long id) {
        Optional<Director> directorOpt = findOne(FIND_BY_ID_QUERY, id);
        if (directorOpt.isPresent()) {
            return directorOpt.get();
        } else {
            throw new NotFoundException("Режиссер с id = " + id + " не найден");

        }
    }

    public Director save(Director director) {
        long id = insert(INSERT_QUERY,
                director.getName());
        director.setId(id);
        return director;
    }

    public void deleteDirector(Long id) {
        delete(DELETE_QUERY, id);
    }

    public Director updateDirector(Director director) {
        if (findOne(FIND_BY_ID_QUERY, director.getId()).isEmpty()) {
            throw new NotFoundException("Режиссер с id = " + director.getId() + " не найден");
        }
        update(UPDATE_QUERY,
                director.getName(),
                director.getId());
        return director;
    }

    public Set<Director> findDirectorsByFilmId(Long filmId) {
        return Set.copyOf(findMany(FIND_DIRECTORS_BY_FILM_ID_QUERY, filmId));
    }
}
