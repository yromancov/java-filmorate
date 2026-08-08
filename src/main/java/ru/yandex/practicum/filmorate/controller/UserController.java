package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import java.util.*;


@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private UserService service;

    @GetMapping
    public Collection<User> findAll() {
        return service.findAll();
    }

    @PostMapping
    public User add(@Valid @RequestBody User user) {
        return service.add(user);
    }

    @GetMapping("/{id}/friends"){

    }

    @GetMapping("{/id}")
    public User getUserById(@PathVariable long id){
        return service.getUser(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable long id, @PathVariable long friendId){
        service.addFriend(id,friendId);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        return service.update(newUser);

    }

}
