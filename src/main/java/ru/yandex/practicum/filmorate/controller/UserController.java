package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.Set;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Collection<User> getUsers() {
        return userService.getUsers();
    }

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        return userService.createUser(user);
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        return userService.updateUser(user);
    }

    @GetMapping(value = "/{id}")
    public User findUser(@PathVariable String id) {
        try {
            return userService.findUser(Long.parseLong(id)).get();
        } catch (NumberFormatException e) {
            throw new ConditionsNotMetException("id пользователя должен быть числом.");
        }
    }

    @GetMapping(value = "/{id}/friends")
    public Set<User> findFriends(@PathVariable String id) {
        try {
            return userService.getFriends(Long.parseLong(id));
        } catch (NumberFormatException e) {
            throw new ConditionsNotMetException("id пользователя должен быть числом.");
        }
    }

    @PutMapping(value = "/{id}/friends/{friendId}")
    public void addFriend(@PathVariable String id,
                          @PathVariable String friendId) {
        try {
            userService.addFriend(Long.parseLong(id), Long.parseLong(friendId));
        } catch (NumberFormatException e) {
            throw new ConditionsNotMetException("id пользователя должен быть числом.");
        }
    }

    @DeleteMapping(value = "/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable String id,
                             @PathVariable String friendId) {
        try {
            userService.deleteFriend(Long.parseLong(id), Long.parseLong(friendId));
        } catch (NumberFormatException e) {
            throw new ConditionsNotMetException("id пользователя должен быть числом.");
        }
    }

    @GetMapping(value = "/{id}/friends/common/{otherId}")
    public Set<User> findCommonFriends(@PathVariable String id,
                                       @PathVariable String otherId) {
        try {
            return userService.findCommonFriends(Long.parseLong(id), Long.parseLong(otherId));
        } catch (NumberFormatException e) {
            throw new ConditionsNotMetException("id пользователя должен быть числом.");
        }
    }
}
