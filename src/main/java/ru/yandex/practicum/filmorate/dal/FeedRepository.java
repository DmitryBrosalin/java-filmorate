package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.FeedRowMapper;
import ru.yandex.practicum.filmorate.model.Feed;

import java.time.Instant;
import java.util.Collection;

@Repository
public class FeedRepository extends BaseRepository<Feed> {

    private static final String INSERT_EVENT_SQL = """
            INSERT INTO user_feed (timestamp, user_id, event_type, operation, entity_id)
            VALUES (?, ?, ?, ?, ?)
        """;

    private static final String SELECT_FEED_BY_USER_SQL = """
            SELECT * FROM user_feed
            WHERE user_id = ?
            ORDER BY timestamp
            LIMIT ?
            OFFSET ?
        """;

    public FeedRepository(JdbcTemplate jdbcTemplate, FeedRowMapper feedRowMapper) {
        super(jdbcTemplate, feedRowMapper);
    }

    public void addEvent(long userId, Feed.EventType eventType, Feed.Operation operation, long entityId) {
        long timestamp = Instant.now().toEpochMilli();
        Object[] params = new Object[] { timestamp, userId, eventType.name(), operation.name(), entityId };
        insert(INSERT_EVENT_SQL, params);
    }

    public Collection<Feed> getUserFeed(long userId, int limit, int offset) {
        return findMany(SELECT_FEED_BY_USER_SQL, userId, limit, offset);
    }
}
