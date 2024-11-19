package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void addFriend(long userId, long friendId) {
        userRepository.addFriend(userId, friendId);
    }

    public void deleteFriend(long userId, long friendId) {
        userRepository.deleteFriend(userId, friendId);
    }

    public Set<User> findCommonFriends(long userId, long otherId) {
        Set<User> otherFriends = getFriends(otherId);
        return getFriends(userId).stream()
                .filter(otherFriends::contains)
                .collect(Collectors.toSet());
    }

    public Set<User> getFriends(long userId) {
        return new HashSet<>(userRepository.findFriends(userId));
    }

    public Collection<User> getUsers() {
        return userRepository.findAll();
    }

    public User findUser(long userId) {
        return userRepository.findById(userId);
    }

    public User createUser(User user) {
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        return userRepository.update(user);
    }
}
