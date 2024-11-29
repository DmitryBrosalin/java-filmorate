package ru.yandex.practicum.filmorate.dal.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dto.UserFilmDto;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserFilmMapper implements RowMapper<UserFilmDto> {
    @Override
    public UserFilmDto mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        UserFilmDto dto = new UserFilmDto();
        dto.setUserId(resultSet.getLong("user_id"));
        dto.setFilmId(resultSet.getLong("film_id"));
        return dto;
    }
}
