package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class Director {
    private Long id;

    @NotBlank(message = "Имя режиссера не может быть пустым")
    private String name;

    public Director() {
    }
}
