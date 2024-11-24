package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FeedRepository;
import ru.yandex.practicum.filmorate.model.Feed;

import java.util.List;

@Service
public class FeedService {

    private final FeedRepository feedRepository;

    public FeedService(FeedRepository feedRepository) {
        this.feedRepository = feedRepository;
    }

    public List<Feed> getFeed(int userId) {
        return feedRepository.getEventsByUserId(userId);
    }

    public void addEvent(Feed feed) {
        if (feed.getEventType() == null || feed.getOperation() == null) {
            throw new IllegalArgumentException("EventType and OperationType must be specified.");
        }
        feedRepository.addEvent(feed);
    }
}

