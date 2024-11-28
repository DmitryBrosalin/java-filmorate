package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dal.FilmRepository;
import ru.yandex.practicum.filmorate.dal.LikesRepository;
import ru.yandex.practicum.filmorate.dal.UserRepository;
import ru.yandex.practicum.filmorate.dto.UserFilmDto;
import ru.yandex.practicum.filmorate.model.Feed;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final FilmRepository filmRepository;
    private final LikesRepository likesRepository;

    public UserService(UserRepository userRepository, FilmRepository filmRepository, LikesRepository likesRepository) {
        this.userRepository = userRepository;
        this.filmRepository = filmRepository;
        this.likesRepository = likesRepository;
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
        if (user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userRepository.save(user);
    }

    public User updateUser(User user) {
        return userRepository.update(user);
    }

    public void deleteUser(long userId) {
        userRepository.deleteUser(userId);
    }

    public List<Film> getRecommendations(long userId) {
        List<UserFilmDto> likesList = likesRepository.getAllLikes();
        HashMap<Long, Set<Long>> userLikesMap = new HashMap<>();

        for (UserFilmDto dto : likesList) {
            userLikesMap.computeIfAbsent(dto.getUserId(), k -> new HashSet<>()).add(dto.getFilmId());
        }

        if (!userLikesMap.containsKey(userId)) {
            return Collections.emptyList();
        }

        Set<Long> targetUserLikes = userLikesMap.get(userId);
        userLikesMap.remove(userId);
        Set<Long> suggestedUserLikes = new HashSet<>();
        int commonSize = -1;

        for (Set<Long> set : userLikesMap.values()) {
            Set<Long> cloneTargetLikes = new HashSet<>(targetUserLikes);
            cloneTargetLikes.retainAll(set);
            if (!cloneTargetLikes.isEmpty()) {
                if (targetUserLikes.size() > commonSize) {
                    suggestedUserLikes = set;
                    commonSize = targetUserLikes.size();
                }
            }
        }
        suggestedUserLikes.removeAll(targetUserLikes);
        return filmRepository.getAllFilmsByIds(suggestedUserLikes);
    }

    public Collection<Feed> getUserFeed(int userId, int limit, int offset) {
        return userRepository.getUserFeed(userId, limit, offset);
    }
}
