package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

@Repository
public class ReviewRepository extends BaseRepository<Review> {
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private static final String INSERT_REVIEW = "INSERT INTO review (content, is_positive, user_id, film_id, useful) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE review SET content = ?, is_positive = ?, user_id = ?, " +
            "film_id = ?, useful = ? WHERE review_id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM review WHERE review_id = ?";
    private static final String DELETE_REVIEW_QUERY = "DELETE FROM review WHERE review_id = ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM review WHERE film_id = ?";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO review_likes (review_id, user_id, like_status) VALUES (?, ?, 1)";
    private static final String UPDATE_USEFUL_PLUS_QUERY = "UPDATE review SET useful = useful + 1 WHERE review_id = ?";
    private static final String UPDATE_USEFUL_MINUS_QUERY = "UPDATE review SET useful = useful - 1 WHERE review_id = ?";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
    private static final String INSERT_DISLIKE_QUERY = "INSERT INTO review_likes (review_id, user_id, like_status) VALUES (?, ?, -1)";
    private static final String FIND_LIKE_STATUS = "SELECT like_status FROM review_likes WHERE review_id = ? AND user_id = ?";

    public ReviewRepository(JdbcTemplate jdbc, RowMapper<Review> mapper, UserRepository userRepository, FilmRepository filmRepository) {
        super(jdbc, mapper);
        this.userRepository = userRepository;
        this.filmRepository = filmRepository;
    }

    public Review addReview(Review review) {
        if (review.getFilmId() == 0
                || review.getUserId() == 0
                || review.getIsPositive() == null) {
            throw new BadRequestException("Некорректное тело запроса");
        }
        if (filmRepository.findById(review.getFilmId()) != null
                && userRepository.findById(review.getUserId()) != null) {
            long id = insert(INSERT_REVIEW,
                    review.getContent(),
                    review.getIsPositive(),
                    review.getUserId(),
                    review.getFilmId(),
                    review.getUseful());
            review.setReviewId(id);
        }
        return review;
    }

    public Review updateReview(Review review) {
        if (review.getFilmId() == 0
                || review.getUserId() == 0
                || review.getIsPositive() == null) {
            throw new BadRequestException("Некорректное тело запроса");
        }
        if (findOne(FIND_BY_ID_QUERY, review.getReviewId()).isEmpty()) {
            throw new NotFoundException("Отзыв с id = " + review.getReviewId() + " не найден.");
        }
        if (filmRepository.findById(review.getFilmId()) != null
                && userRepository.findById(review.getUserId()) != null) {
            update(UPDATE_QUERY,
                    review.getContent(),
                    review.getIsPositive(),
                    review.getUserId(),
                    review.getFilmId(),
                    review.getUseful(),
                    review.getReviewId());
        }
        return review;
    }

    public void removeReview(long id) {
        delete(DELETE_REVIEW_QUERY, id);
    }

    public Collection<Review> getReviews(long filmId) {
        return findMany(FIND_ALL_QUERY, filmId);
    }

    public Review getReview(long id) {
        Optional<Review> revOpt = findOne(FIND_BY_ID_QUERY, id);
        if (revOpt.isPresent()) {
            return revOpt.get();
        } else {
            throw new NotFoundException("Отзыв с id = " + id + " не найден");
        }
    }

    public Review addLike(long reviewId, long userId) {
        try {
            insertPair(INSERT_LIKE_QUERY, reviewId, userId);
        } catch (RuntimeException e) {
            int likeStatus = jdbc.queryForObject(FIND_LIKE_STATUS, Integer.class, reviewId, userId);
            if (likeStatus == 1) {
                throw new BadRequestException("Пользователь " + userId + " уже поставил лайк отзыву " + reviewId);
            } else if (likeStatus == -1) {
                removeDislike(reviewId, userId);
            }
        }
        update(UPDATE_USEFUL_PLUS_QUERY, reviewId);
        return getReview(reviewId);
    }

    public Review removeLike(long reviewId, long userId) {
        delete(DELETE_LIKE_QUERY, reviewId, userId);
        update(UPDATE_USEFUL_MINUS_QUERY, reviewId);
        return getReview(reviewId);
    }


    public Review addDislike(long reviewId, long userId) {
        try {
            insertPair(INSERT_DISLIKE_QUERY, reviewId, userId);
        } catch (RuntimeException e) {
            int likeStatus = jdbc.queryForObject(FIND_LIKE_STATUS, Integer.class, reviewId, userId);
            if (likeStatus == 11) {
                throw new BadRequestException("Пользователь " + userId + " уже поставил дизлайк отзыву " + reviewId);
            } else if (likeStatus == 1) {
                removeLike(reviewId, userId);
            }
        }
        update(UPDATE_USEFUL_MINUS_QUERY, reviewId);
        return getReview(reviewId);
    }

    public Review removeDislike(long reviewId, long userId) {
        delete(DELETE_LIKE_QUERY, reviewId, userId);
        update(UPDATE_USEFUL_PLUS_QUERY, reviewId);
        return getReview(reviewId);
    }
}
