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
            ORDER BY timestamp DESC
            LIMIT ?
            OFFSET ?
        """;

    public FeedRepository(JdbcTemplate jdbcTemplate, FeedRowMapper feedRowMapper) {
        super(jdbcTemplate, feedRowMapper);
    }

    public long addEvent(long userId, Feed.EventType eventType, Feed.Operation operation, long entityId) {
        long timestamp = Instant.now().toEpochMilli();
        Object[] params = new Object[] { timestamp, userId, eventType.name(), operation.name(), entityId };
        return insert(INSERT_EVENT_SQL, params);
    }

    public Collection<Feed> getUserFeed(long userId, int limit, int offset) {
        return findMany(SELECT_FEED_BY_USER_SQL, userId, limit, offset);
    }

    public void addLikeEvent(long userId, long entityId) {
        addEvent(userId, Feed.EventType.LIKE, Feed.Operation.ADD, entityId);
    }

    public void removeLikeEvent(long userId, long entityId) {
        addEvent(userId, Feed.EventType.LIKE, Feed.Operation.REMOVE, entityId);
    }

    public void addReviewEvent(long userId, long entityId) {
        addEvent(userId, Feed.EventType.REVIEW, Feed.Operation.ADD, entityId);
    }

    public void updateReviewEvent(long userId, long entityId) {
        addEvent(userId, Feed.EventType.REVIEW, Feed.Operation.UPDATE, entityId);
    }

    public void removeReviewEvent(long userId, long entityId) {
        addEvent(userId, Feed.EventType.REVIEW, Feed.Operation.REMOVE, entityId);
    }

    public void addFriendEvent(long userId, long entityId) {
        addEvent(userId, Feed.EventType.FRIEND, Feed.Operation.ADD, entityId);
    }

    public void removeFriendEvent(long userId, long entityId) {
        addEvent(userId, Feed.EventType.FRIEND, Feed.Operation.REMOVE, entityId);
    }
}
