package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Event;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FeedService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.*;


@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService service;
    private final FeedService feedService;

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

    @GetMapping("/{id}/recommendations")
    public Collection<Film> getRecommendations(@PathVariable long id) {
        return service.getRecommendations(id);
    }


    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable long userId) {
        service.delete(userId);
    }

    @GetMapping("/{id}/feed")
    public List<Event> getFeed(@PathVariable Long id) {
        return feedService.getFeed(id);
    }
}
