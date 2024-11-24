package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.exception.BadRequestException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.service.FeedService;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Repository
public class FilmRepository extends BaseRepository<Film> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM films WHERE film_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO films (title, description, release_date, " +
            "duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE films SET title = ?, description = ?, release_date = ?, " +
            "duration = ?, mpa_id = ? WHERE film_id = ?";
    private static final String INSERT_GENRE_QUERY = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
    private static final String FIND_LIKES_BY_ID_QUERY = "SELECT * FROM users WHERE user_id IN " +
            "(SELECT user_id FROM likes WHERE film_id = ?)";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM likes WHERE (film_id = ? AND user_id = ?)";
    private static final String FIND_POPULAR_QUERY = "SELECT f.FILM_ID , f.TITLE , f.DESCRIPTION , f.RELEASE_DATE , f.DURATION , f.MPA_ID \n" +
            "FROM films AS f\n" +
            "LEFT JOIN likes AS l ON f.FILM_ID = l.FILM_ID\n" +
            "GROUP BY (f.FILM_ID) ORDER BY COUNT (f.FILM_ID) DESC LIMIT ?";
    private static final String FIND_POPULAR_BY_GENRE_AND_YEAR = "SELECT *\n" +
            "FROM films f \n" +
            "JOIN film_genres f2 ON f.film_id = f2.film_id \n" +
            "JOIN genres g ON f2.genre_id = g.genre_id \n" +
            "WHERE EXTRACT(YEAR FROM f.release_date) = ?\n" +
            "AND f2.genre_id = ? AND f.film_id IN (SELECT DISTINCT film_id \n" +
            "FROM likes \n" +
            "GROUP BY (film_id) \n" +
            "ORDER BY COUNT (film_id) DESC \n" +
            "LIMIT ?);";
    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE (film_id = ?)";
    private static final String FIND_COMMON_FILMS = "SELECT *\n" +
            "FROM films AS f\n" +
            "WHERE f.film_id IN (SELECT film_id FROM likes AS l WHERE l.user_id = ?)\n" +
            "AND f.film_id IN (SELECT film_id FROM likes AS l WHERE l.user_id = ?)\n" +
            "AND f.film_id IN (SELECT DISTINCT film_id FROM likes GROUP BY (film_id) ORDER BY COUNT (film_id) DESC);";
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;
    private final MpaRepository mpaRepository;
    private final FeedService feedService;

    public FilmRepository(JdbcTemplate jdbc, FilmRowMapper mapper,
                          GenreRepository genreRepository, UserRepository userRepository,
                          MpaRepository mpaRepository, FeedService feedService) {
        super(jdbc, mapper);
        this.genreRepository = genreRepository;
        this.userRepository = userRepository;
        this.mpaRepository = mpaRepository;
        this.feedService = feedService;
    }

    public Film findById(long filmId) {
        Optional<Film> filmOpt = findOne(FIND_BY_ID_QUERY, filmId);
        if (filmOpt.isPresent()) {
            Film film = filmOpt.get();
            return prepareForResponse(film);
        } else {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден");
        }
    }

    public List<Film> findAll() {
        return findMany(FIND_ALL_QUERY).stream()
                .peek(this::prepareForResponse)
                .collect(Collectors.toList());
    }

    public Film save(Film film) {
        checkFilmMpaAndGenres(film);
        film.setGenres(new TreeSet<>(film.getGenres()));
        long id = insert(INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId());
        film.setId(id);
        insertGenres(id, film.getGenres());
        return prepareForResponse(film);
    }

    public Film update(Film film) {
        if (findOne(FIND_BY_ID_QUERY, film.getId()).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден.");
        }
        checkFilmMpaAndGenres(film);
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

    private Mpa findMpa(int mpaId) {
        return mpaRepository.getMpaById(mpaId);
    }

    public void addLike(long filmId, long userId) {
        try {
            insertPair(INSERT_LIKE_QUERY, filmId, userId);

            feedService.addEvent(new Feed(
                    System.currentTimeMillis(),
                    (int) userId,
                    Feed.EventType.LIKE.name(),
                    Feed.OperationType.ADD.name(),
                    (int) filmId,
                    0
            ));
        } catch (RuntimeException e) {
            throw new BadRequestException("Пользователь " + userId + " уже поставил лайк фильму " + filmId);
        }
    }

    public Collection<Film> getPopularFilms(long size, Integer genreId, Integer year) {
        if (genreId == null || year == null) {
            return findMany(FIND_POPULAR_QUERY, size).stream()
                    .peek(this::prepareForResponse)
                    .collect(Collectors.toList());
        }
        return findMany(FIND_POPULAR_BY_GENRE_AND_YEAR, year, genreId, size).stream()
                .peek(this::prepareForResponse)
                .collect(Collectors.toList());
    }

    public void deleteLike(long filmId, long userId) {
        delete(DELETE_LIKE_QUERY, filmId, userId);

        feedService.addEvent(new Feed(
                System.currentTimeMillis(),
                (int) userId,
                Feed.EventType.LIKE.name(),
                Feed.OperationType.REMOVE.name(),
                (int) filmId,
                0
        ));
    }

    public void deleteFilm(long filmId) {
        delete(DELETE_FILM_QUERY, filmId);
    }

    private Film prepareForResponse(Film film) {
        film.setGenres(new TreeSet<>(findGenres(film.getId())));
        film.setLikes(findLikes(film.getId()));
        film.setMpa(findMpa(film.getMpa().getId()));
        return film;
    }

    private void checkFilmMpaAndGenres(Film film) {
        if (film.getMpa() != null) {
            try {
                findMpa(film.getMpa().getId());
            } catch (RuntimeException e) {
                throw new BadRequestException("id MPA-рейтинга должен быть от 1 до 5.");
            }
        }
        for (Genre genre: film.getGenres()) {
            if (genre != null) {
                try {
                    genreRepository.findById(genre.getId());
                } catch (RuntimeException e) {
                    throw new BadRequestException("id жанра должен быть от 1 до 6.");
                }
            }
        }
    }

    public Collection<Film> getCommonFilms(long userId, long friendId) {
        return findMany(FIND_COMMON_FILMS, userId, friendId).stream()
                .peek(this::prepareForResponse)
                .collect(Collectors.toList());
    }
}
