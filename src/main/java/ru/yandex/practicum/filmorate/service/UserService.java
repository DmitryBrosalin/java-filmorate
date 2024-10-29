package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public void addFriend(long userId, long friendId) {
        if (findUser(userId).isPresent() && findUser(friendId).isPresent()) {
            findFriendsIdSet(userId).add(friendId);
            findFriendsIdSet(friendId).add(userId);
        }
    }

    public void deleteFriend(long userId, long friendId) {
        if (findUser(userId).isPresent() && findUser(friendId).isPresent()) {
            findFriendsIdSet(userId).remove(friendId);
            findFriendsIdSet(friendId).remove(userId);
        }
    }

    public Set<User> findCommonFriends(long userId, long otherId) {
        if (findUser(userId).isPresent() && findUser(otherId).isPresent()) {
            Set<User> otherFriends = getFriends(otherId);
            return getFriends(userId).stream()
                    .filter(otherFriends::contains)
                    .collect(Collectors.toSet());
        } else {
            throw new NotFoundException("Пользователь(и) с  id = " + userId +  " и/или " + otherId + " не найден(ы)");
        }
    }

    public Set<Long> findFriendsIdSet(long userId) {
        return findUser(userId).get().getFriends();
    }

    public Set<User> getFriends(long userId) {
        return findUser(userId).get().getFriends().stream()
                .map(id -> findUser(id))
                .map(user -> user.get())
                .collect(Collectors.toSet());
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public Optional<User> findUser(long userId) {
        return userStorage.findUser(userId);
    }

    public User createUser(User user) {
        return userStorage.createUser(user);
    }

    public User updateUser(User user) {
        return userStorage.updateUser(user);
    }
}
