package ru.yandex.practicum.filmorate.dal;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.*;
import ru.yandex.practicum.filmorate.service.FeedService;

import java.sql.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UserRepository extends BaseRepository<User> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users (name, login, email, birthday) " +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = "UPDATE users SET name = ?, login = ?, email = ?, " +
            "birthday = ? WHERE user_id = ?";
    private static final String INSERT_FRIEND_QUERY = "INSERT INTO friends (user_id, friend_id) " +
            "VALUES (?, ?)";
    private static final String DELETE_FRIEND_QUERY = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_ALL_FRIENDS_QUERY = "SELECT * FROM users WHERE user_id IN " +
            "(SELECT friend_id FROM friends WHERE user_id = ?)";
    private static final String DELETE_USER_QUERY = "DELETE FROM users WHERE user_id = ?";

    private final FeedService feedService;

    public UserRepository(JdbcTemplate jdbc, UserRowMapper mapper, FeedService feedService) {
        super(jdbc, mapper);
        this.feedService = feedService;
    }

    public User findById(long userId) {
        Optional<User> userOpt = findOne(FIND_BY_ID_QUERY, userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            for (User friend : findFriends(userId)) {
                user.getFriends().add(friend.getId());
            }
            return user;
        } else {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден");
        }
    }

    public List<User> findAll() {
        return findMany(FIND_ALL_QUERY).stream()
                .peek(user -> {
                    for (User friend : findFriends(user.getId()))
                        user.getFriends().add(friend.getId());
                })
                .collect(Collectors.toList());
    }

    public User save(User user) {
        long id = insert(INSERT_QUERY,
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                Date.valueOf(user.getBirthday()));
        user.setId(id);
        return user;
    }

    public User update(User user) {
        if (findOne(FIND_BY_ID_QUERY, user.getId()).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден.");
        }
        update(UPDATE_QUERY,
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                user.getBirthday(),
                user.getId());
        return user;
    }

    public void addFriend(long userId, long friendId) {
        if (findOne(FIND_BY_ID_QUERY, userId).isEmpty() || findOne(FIND_BY_ID_QUERY, friendId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " или friendId = " + friendId + " не найден.");
        }

        try {
            insertPair(INSERT_FRIEND_QUERY, userId, friendId);

            feedService.addEvent(new Feed(
                    System.currentTimeMillis(),
                    (int) userId,
                    Feed.EventType.FRIEND,
                    Feed.OperationType.ADD,
                    0,
                    (int) friendId
            ));
        } catch (RuntimeException e) {
            throw new BadRequestException("Пользователь " + userId + " уже добавил в друзья пользователя " + friendId);
        }
    }

    public void deleteFriend(long userId, long friendId) {
        if (findOne(FIND_BY_ID_QUERY, userId).isEmpty() || findOne(FIND_BY_ID_QUERY, friendId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " или friendId = " + friendId + " не найден.");
        }

        delete(DELETE_FRIEND_QUERY, userId, friendId);

        feedService.addEvent(new Feed(
                System.currentTimeMillis(),
                (int) userId,
                Feed.EventType.FRIEND,
                Feed.OperationType.REMOVE,
                0,
                (int) friendId
        ));
    }


    public List<User> findFriends(long userId) {
        if (findOne(FIND_BY_ID_QUERY, userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }
        return findMany(FIND_ALL_FRIENDS_QUERY, userId).stream()
                .peek(user -> {
                    for (User friend : findFriends(user.getId()))
                        user.getFriends().add(friend.getId());
                })
                .collect(Collectors.toList());
    }

    public void deleteUser(long userId) {
        delete(DELETE_USER_QUERY, userId);
    }
}
