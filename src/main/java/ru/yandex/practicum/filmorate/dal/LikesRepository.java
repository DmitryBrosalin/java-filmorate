package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.UserFilmMapper;
import ru.yandex.practicum.filmorate.dto.UserFilmDto;

import java.util.List;

@Repository
public class LikesRepository extends BaseRepository<UserFilmDto> {
    private static final String FIND_ALL_QUERY = "SELECT user_id, film_id FROM likes";

    public LikesRepository(JdbcTemplate jdbc, UserFilmMapper mapper) {
        super(jdbc, mapper);
    }

    public List<UserFilmDto> getAllLikes() {
        return findMany(FIND_ALL_QUERY);
    }

}
