package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Feed;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class FeedRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final String INSERT_FEED_QUERY = """
            INSERT INTO feed (timestamp, user_id, eventType, operation, entity_id) 
            VALUES (?, ?, ?::event_type, ?::operation_type, ?)
            """;

    private static final String SELECT_FEEDS_BY_USER_ID_QUERY = """
            SELECT * 
            FROM feed 
            WHERE user_id = ? 
            ORDER BY timestamp DESC
            """;

    public FeedRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void addEvent(Feed feed) {
        jdbcTemplate.update(
                INSERT_FEED_QUERY,
                feed.getTimestamp(),
                feed.getUserId(),
                feed.getEventType(),
                feed.getOperation(),
                feed.getEntityId()
        );
    }

    public List<Feed> getEventsByUserId(int userId) {
        return jdbcTemplate.query(
                SELECT_FEEDS_BY_USER_ID_QUERY,
                (rs, rowNum) -> mapRowToFeed(rs),
                userId
        );
    }

    private Feed mapRowToFeed(ResultSet rs) throws SQLException {
        return new Feed(
                rs.getTimestamp("timestamp").getTime(),
                rs.getInt("user_id"),
                rs.getString("eventType"),
                rs.getString("operation"),
                rs.getInt("event_id"),
                rs.getInt("entity_id")
        );
    }
}

