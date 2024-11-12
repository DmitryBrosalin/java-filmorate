package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.exception.BadRequestException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class FilmRepository extends BaseRepository<Film> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE film_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO films (title, description, release_date, " +
            "duration, mpa) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET title = ?, description = ?, release_date = ?, " +
            "duration = ?, mpa = ? WHERE film_id = ?";
    private static final String INSERT_GENRE_QUERY = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
    private static final String FIND_LIKES_BY_ID_QUERY = "SELECT * FROM users WHERE user_id IN " +
            "(SELECT user_id FROM likes WHERE film_id = ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE (film_id = ? AND user_id = ?)";
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;

    public FilmRepository(JdbcTemplate jdbc, FilmRowMapper mapper, GenreRepository genreRepository, UserRepository userRepository) {
        super(jdbc, mapper);
        this.genreRepository = genreRepository;
        this.userRepository = userRepository;
    }

    public Film findById(long filmId) {
        Optional<Film> filmOpt = findOne(FIND_BY_ID_QUERY, filmId);
        if (filmOpt.isPresent()) {
            Film film = filmOpt.get();
            film.setGenres(new TreeSet<>(findGenres(filmId)));
            film.setLikes(findLikes(filmId));
            return film;
        } else {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
    }

    public List<Film> findAll() {
        return findMany(FIND_ALL_QUERY).stream()
                .peek(film -> film.setGenres(findGenres(film.getId())))
                .peek(film -> film.setLikes(findLikes(film.getId())))
                .collect(Collectors.toList());
    }

    public Film save(Film film) {
        if ((film.getMpa() != null) && (film.getMpa().getId() < 1 || film.getMpa().getId() > 5)) {
            throw new BadRequestException("id MPA-рейтинга должен быть от 1 до 5.");
        }
        for (Genre genre: film.getGenres()) {
            if ((genre != null) && (genre.getId() < 1 || genre.getId() > 6)) {
                throw new BadRequestException("id жанра должен быть от 1 до 6.");
            }
        }
        film.setGenres(new TreeSet<>(film.getGenres()));
        long id = insert(INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId());
        film.setId(id);
        insertGenres(id, film.getGenres());
        return film;
    }

    public Film update(Film film) {
        if (findOne(FIND_BY_ID_QUERY, film.getId()).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден.");
        }
        if ((film.getMpa() != null) && (film.getMpa().getId() < 1 || film.getMpa().getId() > 5)) {
            throw new BadRequestException("id MPA-рейтинга должен быть от 1 до 5.");
        }
        for (Genre genre: film.getGenres()) {
            if ((genre != null) && (genre.getId() < 1 || genre.getId() > 6)) {
                throw new BadRequestException("id жанра должен быть от 1 до 6.");
            }
        }
        film.setLikes(findLikes(film.getId()));
        update(UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        return film;
    }

    private void insertGenres(long filmId, Set<Genre> genres) {
        if (genres != null) {
            for (Genre genre: genres) {
                try {
                    insertPair(INSERT_GENRE_QUERY, filmId, genre.getId());
                } catch (RuntimeException e) {
                    throw new BadRequestException("У фильма " + filmId + " уже есть жанр " + genre.getId());
                }
            }
        }
    }

    private Set<Genre> findGenres(long filmId) {
        return genreRepository.findGenresById(filmId);
    }

    private Set<Long> findLikes(long filmId) {
        return userRepository.findMany(FIND_LIKES_BY_ID_QUERY, filmId).stream().map(User::getId).collect(Collectors.toSet());
    }

    public void addLike(long filmId, long userId) {
        try {
            insertPair(INSERT_LIKE_QUERY, filmId, userId);
        } catch (RuntimeException e) {
            throw new BadRequestException("Пользователь " + userId + " уже поставил лайк фильму " + filmId);
        }
    }

    public Collection<Film> getPopularFilms(long size) {
        return findAll().stream().sorted(Comparator.reverseOrder()).limit(size).collect(Collectors.toList());
    }

    public void deleteLike(long filmId, long userId) {
        delete(DELETE_LIKE_QUERY, filmId, userId);
    }
}
