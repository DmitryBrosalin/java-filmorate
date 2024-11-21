package ru.yandex.practicum.filmorate.model;

import lombok.*;

//Спринт 12 Отзывы

@Getter
@Setter
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Review {
    private int reviewId;
    private String content;
    private Boolean isPositive;
    private int userId;
    private int filmId;
    private int useful;

    public Integer getUseful() {
        return useful;
    }

    public void setUseful(Integer useful) {
        this.useful = useful;
    }
}
