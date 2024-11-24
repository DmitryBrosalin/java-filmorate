package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Feed {
    private Long timestamp;
    private int userId;
    private String eventType;
    private String operation;
    private int eventId;
    private int entityId;

    public enum EventType {
        LIKE,
        REVIEW,
        FRIEND
    }

    public enum OperationType {
        ADD,
        REMOVE,
        UPDATE
    }
}