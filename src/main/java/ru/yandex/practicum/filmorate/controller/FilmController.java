package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import java.util.Collection;


@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    private final FilmService service;

    @GetMapping
    public Collection<Film> findAll() {
        return service.findAll();
    }

    @PostMapping
    public Film add(@Valid @RequestBody Film film) {
        return service.add(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        return service.update(film);
    }

}