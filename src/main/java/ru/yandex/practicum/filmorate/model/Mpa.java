package ru.yandex.practicum.filmorate.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class Mpa implements Serializable {
    private int id;
    private String name;

    public Mpa(int id) {
        this.id = id;
        switch (id) {
            case (1):
                this.name = "G";
                break;
            case (2):
                this.name = "PG";
                break;
            case (3):
                this.name = "PG-13";
                break;
            case (4):
                this.name = "R";
                break;
            case (5):
                this.name = "NC-17";
                break;
            default:
                this.name = "";
        }
    }

    public Mpa() {
    }
}
