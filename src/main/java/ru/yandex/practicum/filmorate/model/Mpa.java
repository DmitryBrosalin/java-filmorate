package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Mpa implements Serializable {
    private int id;
    private String name;

    public Mpa(int id) {
        this.id = id;
    }

    public Mpa() {
    }
}
