package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
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
    public User findUser(@PathVariable long id) {
        return userService.findUser(id);
    }

    @GetMapping(value = "/{id}/friends")
    public Set<User> findFriends(@PathVariable long id) {
        return userService.getFriends(id);
    }

    @PutMapping(value = "/{id}/friends/{friendId}")
    public void addFriend(@PathVariable long id,
                          @PathVariable long friendId) {
        userService.addFriend(id, friendId);
    }

    @DeleteMapping(value = "/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable long id,
                             @PathVariable long friendId) {
        userService.deleteFriend(id, friendId);
    }

    @GetMapping(value = "/{id}/friends/common/{otherId}")
    public Set<User> findCommonFriends(@PathVariable long id,
                                       @PathVariable long otherId) {
        return userService.findCommonFriends(id, otherId);
    }

    @DeleteMapping(value = "/{id}")
    public void deleteUser(@PathVariable long id) {
        userService.deleteUser(id);
    }
}
