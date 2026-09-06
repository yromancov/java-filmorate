package ru.yandex.practicum.filmorate.controller.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Film add(Film film) {
        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Optional<Film> getFilm(long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film update(Film film) {
        getFilmOrThrow(film.getId());
        films.put(film.getId(), film);
        log.info("Данные фильма с id={} успешно обновлены", film.getId());
        return film;
    }

    @Override
    public void addLike(long filmId, long userId) {
        getFilmOrThrow(filmId).getLikes().add(userId);
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        getFilmOrThrow(filmId).getLikes().remove(userId);
    }

    @Override
    public Collection<Film> getPopular(long count) {
        return films.values().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .toList();
    }

    private Film getFilmOrThrow(long id) {
        return getFilm(id).orElseThrow(() ->
                new NotFoundException("Фильм с id = " + id + " не найден"));
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}