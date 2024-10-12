package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.validation.LoginConstraint;

import java.time.LocalDate;

@Data
public class User {
    private long id;
    @NotBlank
    @Email
    private String email;
    @NotBlank
    @LoginConstraint
    private String login;
    private String name;
    @PastOrPresent
    private LocalDate birthday;
}
