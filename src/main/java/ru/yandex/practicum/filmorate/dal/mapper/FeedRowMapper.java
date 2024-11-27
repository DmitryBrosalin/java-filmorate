package ru.yandex.practicum.filmorate.dal.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Feed;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class FeedRowMapper implements RowMapper<Feed> {

    @Override
    public Feed mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long timestamp = rs.getLong("timestamp");
        int userId = rs.getInt("user_id");

        String eventTypeStr = rs.getString("event_type");
        Feed.EventType eventType;
        try {
            eventType = Feed.EventType.valueOf(eventTypeStr);
        } catch (IllegalArgumentException e) {
            throw new SQLException("Неверный тип события: " + eventTypeStr);
        }

        String operationStr = rs.getString("operation");
        Feed.Operation operation;
        try {
            operation = Feed.Operation.valueOf(operationStr);
        } catch (IllegalArgumentException e) {
            throw new SQLException("Неверный тип операции: " + operationStr);
        }

        int eventId = rs.getInt("event_id");
        int entityId = rs.getInt("entity_id");

        return new Feed(eventId, timestamp, userId, eventType, operation, entityId);
    }
}


