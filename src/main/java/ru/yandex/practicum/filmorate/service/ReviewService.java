package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;

import java.util.Collection;

//Спринт 12 Отзывы

@Service
@Slf4j
public class ReviewService {

    private final ReviewStorage reviewStorage;

    @Autowired
    public ReviewService(ReviewStorage reviewStorage) {
        this.reviewStorage = reviewStorage;
    }

    public Review addwReview(Review review) {
        return reviewStorage.addReview(review);
    }

    public Review updateReview(Review review) {
        return reviewStorage.updateReview(review);
    }

    public void deleteReview(Integer id) {
        reviewStorage.removeReview(id);
    }

    public Collection<Review> getReviews(Integer filmId) {
        return reviewStorage.getReviews(filmId);
    }

    public Review addLikeReview(Review review) {
        return review;
    }

    public Review addDislikeReview(Review review) {
        return review;
    }

    //удалить лайк
    public Review removeLikeReview(Review review) {
        return review;
    }

    //удалить дизлайк
    public Review removeDislikeReview(Review review) {
        return review;
    }

    public Review getReview(int id) {
        return reviewStorage.getReview(id);
    }

    public Review addLike(int reviewId, int userId) {
        return reviewStorage.addLike(reviewId, userId);
    }

    public Review removeLike(int reviewId, int userId) {
        return reviewStorage.removeLike(reviewId, userId);
    }

    public Review addDislike(int reviewId, int userId) {
        return reviewStorage.addDislike(reviewId, userId);
    }

    public Review removeDislike(int reviewId, int userId) {
        return reviewStorage.removeDislike(reviewId, userId);
    }
}
