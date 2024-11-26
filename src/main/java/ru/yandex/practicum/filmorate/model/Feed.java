package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Data
public class Feed {
    private final Long timestamp;
    private final int userId; //long
    private final EventType eventType;
    private final Operation operation;
    private final int eventId;
    private final int entityId;

    public enum EventType {
        LIKE,
        REVIEW,
        FRIEND
    }

    public enum Operation {
        ADD,
        REMOVE,
        UPDATE
    }
}
