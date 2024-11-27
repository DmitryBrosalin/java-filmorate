package ru.yandex.practicum.filmorate.dal.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.FilmDirector;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FilmDirectorRowMapper implements RowMapper<FilmDirector> {
    @Override
    public FilmDirector mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        FilmDirector filmDirector = new FilmDirector();
        filmDirector.setFilmId(resultSet.getLong("film_id"));
        filmDirector.setDirectorId(resultSet.getLong("director_id"));
        return filmDirector;
    }
}
