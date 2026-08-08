package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {

    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос GET /films");
        return films.values();
    }

    @PostMapping
    public Film add(@Valid @RequestBody Film film) {
        log.info("Получен запрос POST /films");

        validateFilm(film);

        film.setId(getNextId());
        films.put(film.getId(), film);

        log.info("Фильм с id={} успешно добавлен", film.getId());
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Получен запрос PUT /films");

        if (film.getId() == null) {
            log.warn("Обновление невозможно: id фильма не указан");
            throw new ValidationException("Id должен быть указан");
        }

        if (!films.containsKey(film.getId())) {
            log.warn("Фильм с id={} не найден", film.getId());
            throw new NotFoundException("Фильм с id = " + film.getId() + " не найден");
        }

        validateFilm(film);

        films.put(film.getId(), film);

        log.info("Данные фильма с id={} успешно обновлены", film.getId());
        return film;
    }

    private void validateFilm(Film film) {
        log.info("Запуск валидации фильма");

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Валидация не пройдена: дата релиза {} раньше 28.12.1895",
                    film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года.");
        }

        log.info("Валидация фильма пройдена успешно");
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