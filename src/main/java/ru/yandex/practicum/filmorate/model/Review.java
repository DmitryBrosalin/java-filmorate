package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Review {
    private long reviewId;
    @NotBlank
    private String content;
    private Boolean isPositive;
    private long userId;
    private long filmId;
    private long useful;
}
