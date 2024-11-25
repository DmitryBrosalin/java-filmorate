package ru.yandex.practicum.filmorate.model;

import lombok.Data;

@Data
public class Review {
    private long reviewId;
    private String content;
    private Boolean isPositive;
    private long userId;
    private long filmId;
    private long useful;
}