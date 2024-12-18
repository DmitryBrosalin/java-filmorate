package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.Optional;

@Repository
public class ReviewRepository extends BaseRepository<Review> {
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final FeedRepository feedRepository;
    private static final String INSERT_REVIEW = "INSERT INTO review (content, is_positive, user_id, film_id, useful) " +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE review SET content = ?, is_positive = ? WHERE review_id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM review WHERE review_id = ?";
    private static final String DELETE_REVIEW_QUERY = "DELETE FROM review WHERE review_id = ?";
    private static final String FIND_ALL_REVIEWS_FOR_FILM_QUERY = "SELECT * FROM review WHERE film_id = ? ORDER BY useful DESC ";
    private static final String LIMIT_QUERY = " LIMIT ?";
    private static final String FIND_ALL_QUERY = "SELECT * FROM review ORDER BY useful DESC ";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO review_likes (review_id, user_id, like_status) VALUES (?, ?, 1)";
    private static final String UPDATE_LIKE_QUERY = "UPDATE review_likes SET like_status = 1 WHERE (review_id = ? AND user_id = ?)";
    private static final String UPDATE_DISLIKE_QUERY = "UPDATE review_likes SET like_status = -1 WHERE (review_id = ? AND user_id = ?)";
    private static final String UPDATE_USEFUL_PLUS_QUERY = "UPDATE review SET useful = useful + 1 WHERE review_id = ?";
    private static final String UPDATE_USEFUL_MINUS_QUERY = "UPDATE review SET useful = useful - 1 WHERE review_id = ?";
    private static final String DELETE_LIKE_QUERY = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
    private static final String INSERT_DISLIKE_QUERY = "INSERT INTO review_likes (review_id, user_id, like_status) VALUES (?, ?, -1)";

    public ReviewRepository(JdbcTemplate jdbc, RowMapper<Review> mapper, UserRepository userRepository, FilmRepository filmRepository, FeedRepository feedRepository) {
        super(jdbc, mapper);
        this.userRepository = userRepository;
        this.filmRepository = filmRepository;
        this.feedRepository = feedRepository;
    }

    public Review addReview(Review review) {
        if (review.getFilmId() == 0
                || review.getUserId() == 0) {
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

            feedRepository.addEvent(review.getUserId(), Feed.EventType.REVIEW, Feed.Operation.ADD, id);
        }
        return review;
    }

    public Review updateReview(Review review) {
        if (review.getFilmId() == 0
                || review.getUserId() == 0) {
            throw new BadRequestException("Некорректное тело запроса");
        }
        Optional<Review> existingReview = findOne(FIND_BY_ID_QUERY, review.getReviewId());
        if (existingReview.isEmpty()) {
            throw new NotFoundException("Отзыв с id = " + review.getReviewId() + " не найден.");
        }
        if (filmRepository.findById(review.getFilmId()) != null
                && userRepository.findById(review.getUserId()) != null) {
            update(UPDATE_QUERY,
                    review.getContent(),
                    review.getIsPositive(),
                    review.getReviewId());

            feedRepository.addEvent(existingReview.get().getUserId(), Feed.EventType.REVIEW,
                    Feed.Operation.UPDATE, existingReview.get().getReviewId());
        }
        return getReview(review.getReviewId());
    }

    public void removeReview(long id) {
        Optional<Review> existingReview = findOne(FIND_BY_ID_QUERY, id);
        if (existingReview.isEmpty()) {
            throw new NotFoundException("Отзыв с id = " + id + " не найден.");
        }
        delete(DELETE_REVIEW_QUERY, id);

        feedRepository.addEvent(existingReview.get().getUserId(), Feed.EventType.REVIEW, Feed.Operation.REMOVE, id);
    }

    public Collection<Review> getReviews(Long filmId, Long count) {
        if (filmId == null) {
            if (count == null) {
                return findMany(FIND_ALL_QUERY);
            } else {
                return findMany(FIND_ALL_QUERY + LIMIT_QUERY, count);
            }
        } else {
            if (count == null) {
                return findMany(FIND_ALL_REVIEWS_FOR_FILM_QUERY, filmId);
            } else {
                return findMany(FIND_ALL_REVIEWS_FOR_FILM_QUERY + LIMIT_QUERY, filmId, count);
            }
        }
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
            update(UPDATE_LIKE_QUERY, reviewId, userId);
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
            update(UPDATE_DISLIKE_QUERY, reviewId, userId);
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
