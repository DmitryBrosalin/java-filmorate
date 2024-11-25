package ru.yandex.practicum.filmorate.dal;

import ru.yandex.practicum.filmorate.exception.BadRequestException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;

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
    private static final String INSERT_FILM_DIRECTORS =
            "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?);";
    private static final String DELETE_FILM_DIRECTORS =
            "DELETE FROM film_directors WHERE (film_id = ? AND director_id = ?);";
    private static final String DELETE_FILM_GENRES =
            "DELETE FROM film_genres WHERE (film_id = ? AND genre_id = ?);";
    private static final String FIND_FILMS_BY_DIRECTOR_ORDER_BY_LIKES =
            "SELECT DISTINCT * FROM films WHERE film_id IN (SELECT F.FILM_ID \n" +
                    "FROM films AS f\n" +
                    "LEFT JOIN likes AS l ON f.film_id = l.film_id\n" +
                    "WHERE f.film_id IN (SELECT fd.film_id\n" +
                    "FROM FILM_DIRECTORS fd\n" +
                    "WHERE fd.DIRECTOR_ID = ?)\n" +
                    "ORDER BY l.user_id DESC);";
    private static final String FIND_FILMS_BY_DIRECTOR_ORDER_BY_RELEASE_DATE =
            "SELECT *\n" +
                    "FROM films AS f\n" +
                    "WHERE film_id IN (SELECT film_id\n" +
                    "FROM FILM_DIRECTORS fd\n" +
                    "WHERE fd.DIRECTOR_ID = ?)\n" +
                    "ORDER BY f.RELEASE_DATE;";
    private static final String FIND_FILMS_BY_DIRECTOR_ID =
            "SELECT *\n" +
                    "FROM films AS f\n" +
                    "WHERE film_id IN (SELECT film_id\n" +
                    "FROM FILM_DIRECTORS fd\n" +
                    "WHERE fd.DIRECTOR_ID = ?)";
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;
    private final MpaRepository mpaRepository;
    private final DirectorRepository directorRepository;

    public FilmRepository(JdbcTemplate jdbc, FilmRowMapper mapper, GenreRepository genreRepository,
                          UserRepository userRepository, MpaRepository mpaRepository, DirectorRepository directorRepository) {
        super(jdbc, mapper);
        this.genreRepository = genreRepository;
        this.userRepository = userRepository;
        this.mpaRepository = mpaRepository;
        this.directorRepository = directorRepository;
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
        insertDirectors(id, film.getDirectors());
        return prepareForResponse(film);
    }

    public Film update(Film film) {
        if (findOne(FIND_BY_ID_QUERY, film.getId()).isEmpty()) {
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден.");
        }
        checkFilmMpaAndGenres(film);
        if (!film.getDirectors().equals(findById(film.getId()).getDirectors())) {
            deleteFilmDirectors(film.getId(), findById(film.getId()).getDirectors());
            insertDirectors(film.getId(), film.getDirectors());
        }
        if (!film.getGenres().equals(findById(film.getId()).getGenres())) {
            deleteFilmGenres(film.getId(), findById(film.getId()).getGenres());
            insertGenres(film.getId(), film.getGenres());
        }
        film.setLikes(findLikes(film.getId()));
        update(UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());
        return prepareForResponse(film);
    }

    private void deleteFilmDirectors(long filmId, Set<Director> directors) {
        for (Director director : directors) {
            delete(DELETE_FILM_DIRECTORS, filmId, director.getId());
        }
    }

    private void deleteFilmGenres(long filmId, Set<Genre> genres) {
        for (Genre genre: genres) {
            delete(DELETE_FILM_GENRES, filmId, genre.getId());
        }
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

    private void insertDirectors(long filmId, Set<Director> directors) {
        if (directors != null) {
            for (Director director : directors) {
                try {
                    insertPair(INSERT_FILM_DIRECTORS, filmId, director.getId());
                } catch (RuntimeException e) {
                    throw new BadRequestException("У фильма " + filmId + " уже есть режиссер" + director.getId());
                }
            }
        }
    }

    private Set<Director> findDirectors(long filmId) {
        return directorRepository.findDirectorsByFilmId(filmId);
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
    }

    public void deleteFilm(long filmId) {
        delete(DELETE_FILM_QUERY, filmId);
    }

    private Film prepareForResponse(Film film) {
        film.setGenres(new TreeSet<>(findGenres(film.getId())));
        film.setLikes(findLikes(film.getId()));
        film.setMpa(findMpa(film.getMpa().getId()));
        film.setDirectors(new LinkedHashSet<>(findDirectors(film.getId())));
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

    public Collection<Film> getAllFilmsByIds(Set<Long> filmIds) {
        StringBuilder queryStart = new StringBuilder("SELECT * FROM films WHERE film_id IN (");
        List<Long> filmsFromSet = new ArrayList<>(filmIds);

        if (filmsFromSet.isEmpty()) {
            return Collections.emptyList();
        }

        for (int i = 0; i < filmsFromSet.size(); i++) {
            if (i < filmsFromSet.size() - 1) {
                queryStart.append(filmsFromSet.get(i)).append(",");
            } else {
                queryStart.append(filmsFromSet.get(i)).append(")");
            }
        }
        return findMany(queryStart.toString()).stream()
                .peek(this::prepareForResponse)
                .collect(Collectors.toList());
    }

    public Collection<Film> getFilmsByDirector(long directorId, String sortBy) {
        if (sortBy.equals("year")) {
            return findMany(FIND_FILMS_BY_DIRECTOR_ORDER_BY_RELEASE_DATE, directorId).stream()
                    .peek(this::prepareForResponse)
                    .collect(Collectors.toList());
        } else {
            return findMany(FIND_FILMS_BY_DIRECTOR_ORDER_BY_LIKES, directorId).stream()
                    .peek(this::prepareForResponse)
                    .collect(Collectors.toList());
        }
    }

    public List<Film> findFilms(String findQuery) {
        return findMany(findQuery).stream()
                .peek(this::prepareForResponse)
                .collect(Collectors.toList());
    }

    public Collection<Film> getAllFilmsByDirectors(List<Director> directorsIds) {
        if (directorsIds.size() == 1) {
            return findMany(FIND_FILMS_BY_DIRECTOR_ID, directorsIds.getFirst().getId()).stream()
                    .peek(this::prepareForResponse)
                    .collect(Collectors.toList());
        }
        else {
            StringBuilder idsString = new StringBuilder();
            for (int i = 0; i < directorsIds.size(); i++) {
                if (i == directorsIds.size() - 1) {
                    idsString.append(directorsIds.get(i).getId());
                } else {
                    idsString.append(directorsIds.get(i).getId()).append(",");
                }
            }

            String FIND_FILMS_BY_DIRECTOR_IDS = "SELECT * FROM films AS f " +
                    "WHERE film_id IN (SELECT film_id FROM FILM_DIRECTORS fd " +
                    "WHERE fd.DIRECTOR_ID IN (" + idsString + "))";

            return findMany(FIND_FILMS_BY_DIRECTOR_IDS).stream()
                    .peek(this::prepareForResponse)
                    .collect(Collectors.toList());
        }
    }
}
