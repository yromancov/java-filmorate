package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage storage;
    private final UserService userService;

    public FilmService(FilmStorage storage, UserService service) {
        this.storage = storage;
        this.userService = service;
    }

    public Collection<Film> findAll() {
        log.info("Получен запрос GET /films");
        return storage.findAll();
    }

    public Film getFilm(long id) {
        return storage.getFilm(id);
    }

    public void addLike(long id, long userId) {
        Film film = storage.getFilm(id);
        userService.getUser(userId);
        film.getLikes().add(userId);
        storage.update(film);
        log.info("Пользователь с id={} поставил лайк фильму с id={}", userId, id);


    }

    public void deleteLike(long id, long userId) {
        Film film = storage.getFilm(id);
        userService.getUser(userId);
        film.getLikes().remove(userId);
        storage.update(film);
        log.info("Пользователь с id={} удалил лайк фильму с id={}", userId, id);
    }

    public Collection<Film> listOfTopFilmsByCount(long count) {
        Comparator<Film> comparator = Comparator.comparingInt((Film film) -> film.getLikes().size())
                .reversed();
        return findAll().stream()
                .sorted(comparator)
                .limit(count)
                .toList();
    }

    public Film add(Film film) {
        log.info("Получен запрос POST /films");
        validateFilm(film);
        Film saved = storage.add(film);
        log.info("Фильм с id={} успешно добавлен", saved.getId());
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
