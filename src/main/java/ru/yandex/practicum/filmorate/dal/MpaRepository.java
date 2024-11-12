package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

@Repository
public class MpaRepository extends BaseRepository<Mpa> {
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa_rating WHERE mpa = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa_rating";

    public MpaRepository(JdbcTemplate jdbc, MpaRowMapper mapper) {
        super(jdbc, mapper);
    }

    public Collection<Mpa> getAllMpa() {
        return findMany(FIND_ALL_QUERY);
    }

    public Mpa getMpaById(int id) {
        Optional<Mpa> mpaOpt = findOne(FIND_BY_ID_QUERY, id);
        if (mpaOpt.isPresent()) {
            return mpaOpt.get();
        } else {
            throw new NotFoundException("MPA с id = " + id + " не найден.");
        }
    }
}
