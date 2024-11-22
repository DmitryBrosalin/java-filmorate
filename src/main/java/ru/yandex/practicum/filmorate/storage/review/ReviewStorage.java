package ru.yandex.practicum.filmorate.storage.review;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;
import java.util.List;

@Repository
@Qualifier("reviewStorage")
@Slf4j
@Component
public class ReviewStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public ReviewStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Review addReview(Review review) {
        if (review.getUserId() == 0) {
            throw new ConditionsNotMetException("User is null or invalid.");
        }
        if (review.getFilmId() == 0) {
            throw new ConditionsNotMetException("Film is null or invalid.");
        }
        if (review.getFilmId() <= 0) {
            throw new NotFoundException("Film is null or invalid.");
        }
        String userCheckSql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        Integer userCount = jdbcTemplate.queryForObject(userCheckSql, Integer.class, review.getUserId());
        if (userCount == null || userCount == 0) {
            throw new NotFoundException("User with ID " + review.getUserId() + " does not exist.");
        }
        String filmCheckSql = "SELECT COUNT(*) FROM films WHERE film_id = ?";
        Integer filmCount = jdbcTemplate.queryForObject(filmCheckSql, Integer.class, review.getFilmId());
        if (filmCount == null || filmCount == 0) {
            throw new NotFoundException("Film with ID " + review.getFilmId() + " does not exist.");
        }
        if (review.getIsPositive() == null) {
            throw new IllegalArgumentException("isPositive cannot be null.");
        }
        String sql = "INSERT INTO review (content, is_positive, user_id, film_id, useful) " +
                "VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            var preparedStatement = connection.prepareStatement(sql, new String[]{"review_id"});
            preparedStatement.setString(1, review.getContent());
            preparedStatement.setObject(2, review.getIsPositive() != null ? review.getIsPositive() : null);
            preparedStatement.setInt(3, review.getUserId());
            preparedStatement.setInt(4, review.getFilmId());
            preparedStatement.setObject(5, review.getUseful() != null ? review.getUseful() : null);
            return preparedStatement;
        }, keyHolder);
        review.setReviewId(keyHolder.getKey().intValue());
        review.setUseful(0);
        return review;
    }


    public Review updateReview(Review review) {
        System.out.println(review.getUseful());
        String reviewCheckSql = "SELECT COUNT(*) FROM review WHERE review_id = ?";
        Integer reviewCount = jdbcTemplate.queryForObject(reviewCheckSql, Integer.class, review.getReviewId());
        if (reviewCount == null || reviewCount == 0) {
            throw new IllegalArgumentException("Review with ID " + review.getReviewId() + " does not exist.");
        }
        Integer usefulValue = review.getUseful() != null ? review.getUseful() : 0;
        String sql = "UPDATE review SET content = ?, is_positive = ?, useful = ? WHERE review_id = ?";
        int rowsAffected = jdbcTemplate.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getUseful(),
                review.getReviewId());
        if (rowsAffected > 0) {
            System.out.println(review.getUseful());
            return review;
        } else {
            return null;
        }
    }

    public Collection<Review> getReviews(Integer filmId) {
        String sql;
        List<Review> reviews;
        if (filmId != null) {
            sql = "SELECT review_id, content, is_positive, user_id, film_id, useful FROM review WHERE film_id = ?";
            reviews = jdbcTemplate.query(sql, (rs, rowNum) ->
                    new Review(
                            rs.getInt("review_id"),
                            rs.getString("content"),
                            rs.getBoolean("is_positive"),
                            rs.getInt("user_id"),
                            rs.getInt("film_id"),
                            rs.getInt("useful")
                    ), filmId);
        } else {
            sql = "SELECT review_id, content, is_positive, user_id, film_id, useful FROM review";
            reviews = jdbcTemplate.query(sql, (rs, rowNum) ->
                    new Review(
                            rs.getInt("review_id"),
                            rs.getString("content"),
                            rs.getBoolean("is_positive"),
                            rs.getInt("user_id"),
                            rs.getInt("film_id"),
                            rs.getInt("useful")
                    ));
        }
        return reviews;
    }

    public void removeReview(Integer id) {
        String reviewCheckSql = "SELECT COUNT(*) FROM review WHERE review_id = ?";
        Integer reviewCount = jdbcTemplate.queryForObject(reviewCheckSql, Integer.class, id);
        if (reviewCount == null || reviewCount == 0) {
            throw new IllegalArgumentException("Review with ID " + id + " does not exist.");
        }
        String sql = "DELETE FROM review WHERE review_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, id);
        if (rowsAffected == 0) {
            throw new IllegalStateException("Failed to delete review with ID " + id);
        }
    }


    public Review getReview(int id) {
        String sql = "SELECT review_id, content, is_positive, user_id, film_id, useful " +
                "FROM review WHERE review_id = ?";

        try {
            return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> {
                // Создаём и возвращаем объект Review из результата запроса
                Review review = new Review();
                review.setReviewId(rs.getInt("review_id"));
                review.setContent(rs.getString("content"));
                review.setIsPositive(rs.getBoolean("is_positive"));
                review.setUserId(rs.getInt("user_id"));
                review.setFilmId(rs.getInt("film_id"));
                review.setUseful(rs.getInt("useful"));
                return review;
            });
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("Review with ID " + id + " does not exist.");
        }
    }

    public Review addLike(int reviewId, int userId) {
        String checkLikeSql = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?";
        Integer likeCount = jdbcTemplate.queryForObject(checkLikeSql, Integer.class, reviewId, userId);
        if (likeCount != null && likeCount > 0) {
            throw new IllegalArgumentException("User has already liked this review.");
        }
        String addLikeSql = "INSERT INTO review_likes (review_id, user_id, like_status) VALUES (?, ?, 1)";
        jdbcTemplate.update(addLikeSql, reviewId, userId);
        String updateUsefulSql = "UPDATE review SET useful = COALESCE(useful, 0) + 1 WHERE review_id = ?";
        jdbcTemplate.update(updateUsefulSql, reviewId);
        String getReviewSql = "SELECT review_id, content, is_positive, user_id, film_id, useful FROM review WHERE review_id = ?";
        Review review = jdbcTemplate.queryForObject(getReviewSql, new Object[]{reviewId}, (rs, rowNum) -> {
            Review r = new Review();
            r.setReviewId(rs.getInt("review_id"));
            r.setContent(rs.getString("content"));
            r.setIsPositive(rs.getBoolean("is_positive"));
            r.setUserId(rs.getInt("user_id"));
            r.setFilmId(rs.getInt("film_id"));
            r.setUseful(rs.getInt("useful"));
            return r;
        });

        return review;
    }

    public Review removeLike(int reviewId, int userId) {
        String checkLikeSql = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?";
        Integer likeCount = jdbcTemplate.queryForObject(checkLikeSql, Integer.class, reviewId, userId);
        if (likeCount == null || likeCount == 0) {
            throw new IllegalArgumentException("Like not found for reviewId " + reviewId + " and userId " + userId);
        }
        String deleteLikeSql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
        jdbcTemplate.update(deleteLikeSql, reviewId, userId);
        String updateUsefulSql = "UPDATE review SET useful = COALESCE(useful, 0) - 1 WHERE review_id = ?";
        jdbcTemplate.update(updateUsefulSql, reviewId);
        String getReviewSql = "SELECT review_id, content, is_positive, user_id, film_id, useful FROM review WHERE review_id = ?";
        Review review = jdbcTemplate.queryForObject(getReviewSql, new Object[]{reviewId}, (rs, rowNum) -> {
            Review r = new Review();
            r.setReviewId(rs.getInt("review_id"));
            r.setContent(rs.getString("content"));
            r.setIsPositive(rs.getBoolean("is_positive"));
            r.setUserId(rs.getInt("user_id"));
            r.setFilmId(rs.getInt("film_id"));
            r.setUseful(rs.getInt("useful"));
            return r;
        });
        return review;
    }

    public Review addDislike(int reviewId, int userId) {
        String checkLikeSql = "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?";
        Integer likeCount = jdbcTemplate.queryForObject(checkLikeSql, Integer.class, reviewId, userId);

        if (likeCount != null && likeCount > 0) {
            String updateDislikeSql = "UPDATE review_likes SET like_status = -1 WHERE review_id = ? AND user_id = ?";
            jdbcTemplate.update(updateDislikeSql, reviewId, userId);
        } else {
            String insertDislikeSql = "INSERT INTO review_likes (review_id, user_id, like_status) VALUES (?, ?, -1)";
            jdbcTemplate.update(insertDislikeSql, reviewId, userId);
        }
        String updateUsefulSql = "UPDATE review SET useful = COALESCE(useful, 0) - 2 WHERE review_id = ?";
        int updatedRows = jdbcTemplate.update(updateUsefulSql, reviewId);
        if (updatedRows == 0) {
            log.warn("Ошибка обновления useful. Возможно, запись не найдена.");
        }
        String getReviewSql = "SELECT review_id, content, is_positive, user_id, film_id, useful FROM review WHERE review_id = ?";
        Review review = jdbcTemplate.queryForObject(getReviewSql, new Object[]{reviewId}, (rs, rowNum) -> {
            Review r = new Review();
            r.setReviewId(rs.getInt("review_id"));
            r.setContent(rs.getString("content"));
            r.setIsPositive(rs.getBoolean("is_positive"));
            r.setUserId(rs.getInt("user_id"));
            r.setFilmId(rs.getInt("film_id"));
            r.setUseful(rs.getInt("useful"));
            return r;
        });

        return review;
    }


    public Review removeDislike(int reviewId, int userId) {
        String deleteDislikeSql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ? AND like_status = -1";
        jdbcTemplate.update(deleteDislikeSql, reviewId, userId);
        String updateUsefulSql = "UPDATE review SET useful = useful + 1 WHERE review_id = ?";
        jdbcTemplate.update(updateUsefulSql, reviewId);
        String getReviewSql = "SELECT review_id, content, is_positive, user_id, film_id, useful FROM review WHERE review_id = ?";
        Review review = jdbcTemplate.queryForObject(getReviewSql, new Object[]{reviewId}, (rs, rowNum) -> {
            Review r = new Review();
            r.setReviewId(rs.getInt("review_id"));
            r.setContent(rs.getString("content"));
            r.setIsPositive(rs.getBoolean("is_positive"));
            r.setUserId(rs.getInt("user_id"));
            r.setFilmId(rs.getInt("film_id"));
            r.setUseful(rs.getInt("useful"));
            return r;
        });
        return review;
    }
}
