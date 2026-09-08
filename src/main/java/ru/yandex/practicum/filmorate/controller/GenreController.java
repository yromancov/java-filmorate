package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.controller.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@RestController
@RequestMapping("/genres")
@Slf4j
@RequiredArgsConstructor
public class GenreController {
    private final GenreDbStorage storage;

    @GetMapping
    public Collection<Genre> findAll() {
        return storage.findAll();
    }

    @GetMapping("/{id}")
    public Genre getGenre(@PathVariable int id) {
        return storage.findById(id).orElseThrow(() ->
                new NotFoundException("Жанр с id = " + id + " не найден"));
    }
}

