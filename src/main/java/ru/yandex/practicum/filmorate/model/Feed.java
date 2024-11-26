package ru.yandex.practicum.filmorate.model;

import lombok.*;

@Data
public class Feed {
    private final Long timestamp;
    private final long userId;
    private final EventType eventType;
    private final Operation operation;
    private final long eventId;
    private final long entityId;

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

