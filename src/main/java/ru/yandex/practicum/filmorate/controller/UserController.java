package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.*;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;

    @GetMapping
    public Collection<User> findAll() {
        return service.findAll();
    }

    @PostMapping
    public User add(@Valid @RequestBody User user) {
        return service.add(user);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriends(@PathVariable long id) {
        return service.getFriends(id);
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable long id) {
        return service.getUser(id);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriendById(@PathVariable long id, @PathVariable long friendId) {
        service.deleteFriendById(id, friendId);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        return service.getCommonFriends(id, otherId);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable long id, @PathVariable long friendId) {
        service.addFriend(id, friendId);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        return service.update(newUser);

    }

}
