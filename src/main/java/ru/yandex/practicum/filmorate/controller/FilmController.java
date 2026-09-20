package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;


@RestController
@RequestMapping("/films")
@RequiredArgsConstructor
public class FilmController {
    private final FilmService service;

    @GetMapping
    public Collection<Film> findAll() {
        return service.findAll();
    }

    @GetMapping("/common")
    public Collection<Film> getCommonFilms(@RequestParam long userId,
                                           @RequestParam long friendId) {
        return service.getCommonFilms(userId, friendId);
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable long id) {
        return service.getFilm(id);
    }

    @PostMapping
    public Film add(@Valid @RequestBody Film film) {
        return service.add(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        return service.update(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable long id, @PathVariable long userId) {
        service.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable long id, @PathVariable long userId) {
        service.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> listOfTopFilmsByCount(@RequestParam(defaultValue = "10") long count) {
        return service.listOfTopFilmsByCount(count);
    }

    @DeleteMapping("/{filmId}")
    public void deleteFilm(@PathVariable long filmId) {
        service.delete(filmId);
    }

    @GetMapping("/director/{directorId}")
    public Collection<Film> getPopularFilmsByDirectorId(@PathVariable int directorId, @RequestParam String sortBy) {
        return service.getPopularFilmsByDirectorId(directorId, sortBy);
    }

    @GetMapping("/search")
    public Collection<Film> searchByTitleOrDirector(@RequestParam String query, @RequestParam String by) {
        return service.searchByTitleOrDirector(query, by);
    }

}