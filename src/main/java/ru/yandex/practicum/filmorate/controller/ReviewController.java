package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.Collection;

//Спринт 12 Отзывы

@Slf4j
@RestController
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping
    public Review addwReview(@RequestBody Review review) {
        return reviewService.addwReview(review);
    }

    @PutMapping
    public Review updateReview(@RequestBody Review review) {
        return reviewService.updateReview(review);
    }

    @GetMapping
    public Collection<Review> getReviews(@RequestParam(value = "filmId", required = false) Integer filmId) {
        return reviewService.getReviews(filmId);
    }

    @DeleteMapping("/{id}")
    public void removeReview(@PathVariable("id") int id) {
        reviewService.deleteReview(id);
    }

    @GetMapping("/{id}")
    public Review getReview(@PathVariable("id") int id) {
        return reviewService.getReview(id);
    }

    @PutMapping("/{reviewId}/like/{userId}")
    public Review addLike(@PathVariable("reviewId") int reviewId, @PathVariable("userId") int userId) {
        return reviewService.addLike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/like/{userId}")
    public Review removeLike(@PathVariable("reviewId") int reviewId, @PathVariable("userId") int userId) {
        return reviewService.removeLike(reviewId, userId);
    }

    @PutMapping("/{reviewId}/dislike/{userId}")
    public Review addDislike(@PathVariable("reviewId") int reviewId, @PathVariable("userId") int userId) {
        return reviewService.addDislike(reviewId, userId);
    }

    @DeleteMapping("/{reviewId}/dislike/{userId}")
    public Review removeDislike(@PathVariable("reviewId") int reviewId, @PathVariable("userId") int userId) {
        return reviewService.removeDislike(reviewId, userId);
    }

}
