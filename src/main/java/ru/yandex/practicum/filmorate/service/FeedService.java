package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FeedRepository;
import ru.yandex.practicum.filmorate.model.Feed;

import java.util.Collection;


@Service
@RequiredArgsConstructor
public class FeedService {

    private final FeedRepository feedRepository;

    public long addEvent(int userId, Feed.EventType eventType, Feed.Operation operation, long entityId) {
        return feedRepository.addEvent(userId, eventType, operation, entityId);
    }

    public Collection<Feed> getUserFeed(int userId, int limit, int offset) {
        return feedRepository.getUserFeed(userId, limit, offset);
    }
}
