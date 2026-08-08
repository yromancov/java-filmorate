package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage storage;

    public FilmService(FilmStorage storage) {
        this.storage = storage;
    }

    public Collection<Film> findAll() {
        log.info("Получен запрос GET /films");
        return storage.findAll();
    }
    public Film add(Film film) {
        log.info("Получен запрос POST /films");
        validateFilm(film);
        Film saved = storage.add(film);
        log.info("Фильм с id={} успешно добавлен", film.getId());
        return saved;
    }

    public Film update(Film film) {
        log.info("Получен запрос PUT /films");

        if (film.getId() == null) {
            log.warn("Обновление невозможно: id фильма не указан");
            throw new ValidationException("Id должен быть указан");
        }
        validateFilm(film);
        Film saved = storage.update(film);
        log.info("Данные фильма с id={} успешно обновлены", film.getId());
        return saved;
    }

    public void validateFilm(Film film) {
        log.info("Запуск валидации фильма");
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Валидация не пройдена: дата релиза {} раньше 28.12.1895",
                    film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года.");
        }
        log.info("Валидация фильма пройдена успешно");
    }
}
