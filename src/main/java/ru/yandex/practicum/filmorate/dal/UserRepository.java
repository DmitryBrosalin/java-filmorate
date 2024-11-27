package ru.yandex.practicum.filmorate.dal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dal.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.exception.BadRequestException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.util.ArrayList;
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
    private static final String FIND_FRIENDS_ID_QUERY = "SELECT friend_id FROM friends WHERE user_id = ?";
    private static final String DELETE_USER_QUERY = "DELETE FROM users WHERE user_id = ?";

    private final FeedRepository feedRepository;

    public UserRepository(JdbcTemplate jdbc, UserRowMapper mapper, FeedRepository feedRepository) {
        super(jdbc, mapper);
        this.feedRepository = feedRepository;
    }

    public User findById(long userId) {
        Optional<User> userOpt = findOne(FIND_BY_ID_QUERY, userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            for (User friend: findFriends(userId)) {
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
                    for (User friend: findFriends(user.getId())) user.getFriends().add(friend.getId()); })
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
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }

        try {
            insertPair(INSERT_FRIEND_QUERY, userId, friendId);
            feedRepository.addFriendEvent(userId, friendId);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Пользователь " + userId + " уже добавил в друзья пользователя " + friendId);
        } catch (RuntimeException e) {
            throw new InternalServerException("Ошибка при добавлении друга пользователя " + friendId + " пользователем " + userId);
        }
    }

    public void deleteFriend(long userId, long friendId) {
        if (findOne(FIND_BY_ID_QUERY, userId).isEmpty() || findOne(FIND_BY_ID_QUERY, friendId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }
        delete(DELETE_FRIEND_QUERY, userId, friendId);
        feedRepository.removeFriendEvent(userId, friendId);
    }

    public List<User> findFriends(long userId) {
        if (findOne(FIND_BY_ID_QUERY, userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }
        return findMany(FIND_ALL_FRIENDS_QUERY, userId).stream()
                .peek(user -> {
                    for (long friendId: findFriendsId(user.getId())) user.getFriends().add(friendId); })
                .collect(Collectors.toList());
    }

    private List<Long> findFriendsId(long userId) {
        if (findOne(FIND_BY_ID_QUERY, userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }
        try {
            return jdbc.queryForObject(FIND_FRIENDS_ID_QUERY, List.class, userId);
        } catch (EmptyResultDataAccessException ignored) {
            return new ArrayList<>();
        }
    }

    public void deleteUser(long userId) {
        delete(DELETE_USER_QUERY, userId);
    }
}
