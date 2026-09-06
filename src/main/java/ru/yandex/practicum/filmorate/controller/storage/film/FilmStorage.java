package ru.yandex.practicum.filmorate.controller.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    Collection<Film> findAll();

    Film add(Film film);

    Film update(Film film);

    Optional<Film> getFilm(long id);

    void addLike(long filmId, long userId);

    void deleteLike(long filmId, long userId);

    Collection<Film> getPopular(long count);
}
