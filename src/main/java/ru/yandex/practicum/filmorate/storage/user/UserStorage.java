package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Collection<User> getUsers();

    Optional<User> findUser(long id);

    User createUser(User user);

    User updateUser(User user);

    Long getNextId();
}

