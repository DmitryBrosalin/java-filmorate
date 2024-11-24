package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.service.FeedService;

import java.util.List;

@RestController
@RequestMapping("/users/{userId}/feed")
public class FeedController {

    private final FeedService feedService;

    public FeedController(FeedService feedService) {
        this.feedService = feedService;
    }

    @GetMapping
    public ResponseEntity<List<Feed>> getUserFeed(@PathVariable int userId) {
        List<Feed> feed = feedService.getFeed(userId);

        if (feed.isEmpty()) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(feed);
    }

    @PostMapping
    public ResponseEntity<String> addFeedEvent(@RequestBody Feed feed) {
        try {
            feedService.addEvent(feed);
            return ResponseEntity.status(HttpStatus.CREATED).body("Event successfully added.");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
        }
    }
}
