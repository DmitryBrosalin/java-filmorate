package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.ReviewRepository;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.Collection;

@Service
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public Review addReview(Review review) {
        return reviewRepository.addReview(review);
    }

    public Review updateReview(Review review) {
        return reviewRepository.updateReview(review);
    }

    public void deleteReview(long id) {
        reviewRepository.removeReview(id);
    }

    public Collection<Review> getReviews(long filmId) {
        return reviewRepository.getReviews(filmId);
    }

    public Review getReview(long id) {
        return reviewRepository.getReview(id);
    }

    public Review addLike(long reviewId, long userId) {
        return reviewRepository.addLike(reviewId, userId);
    }

    public Review removeLike(long reviewId, long userId) {
        return reviewRepository.removeLike(reviewId, userId);
    }

    public Review addDislike(long reviewId, long userId) {
        return reviewRepository.addDislike(reviewId, userId);
    }

    public Review removeDislike(long reviewId, long userId) {
        return reviewRepository.removeDislike(reviewId, userId);
    }
}
