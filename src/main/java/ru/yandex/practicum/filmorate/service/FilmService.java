package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage storage;
    private final UserService userService;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final DirectorStorage directorStorage;

    public FilmService(FilmStorage storage,
                       UserService userService,
                       MpaDbStorage mpaStorage,
                       GenreDbStorage genreStorage, DirectorStorage directorStorage) {
        this.storage = storage;
        this.userService = userService;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.directorStorage = directorStorage;
    }

    public Collection<Film> findAll() {
        log.info("Получен запрос GET /films");
        return storage.findAll();
    }

    public Film getFilm(long id) {
        return storage.getFilm(id).orElseThrow(() ->
                new NotFoundException("Фильм с id = " + id + " не найден"));
    }

    public void addLike(long id, long userId) {
        getFilm(id);
        userService.getUser(userId);
        storage.addLike(id, userId);
        log.info("Пользователь с id={} поставил лайк фильму с id={}", userId, id);


    }

    public void deleteLike(long id, long userId) {
        getFilm(id);
        userService.getUser(userId);
        storage.deleteLike(id, userId);
        log.info("Пользователь с id={} удалил лайк фильму с id={}", userId, id);
    }

    public Collection<Film> listOfTopFilmsByCount(long count) {
        return storage.getPopular(count);
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
        getFilm(film.getId());
        validateFilm(film);
        Film saved = storage.update(film);
        log.info("Данные фильма с id={} успешно обновлены", film.getId());
        return saved;
    }

    public void delete(long id) {
        log.info("Получен запрос DELETE /films/{}", id);
        if (!storage.delete(id)) {
            throw new NotFoundException("Фильм с id = " + id + " не найден");
        }
        log.info("Фильм с id={} успешно удалён", id);
    }

    public void validateFilm(Film film) {
        log.info("Запуск валидации фильма");

        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            log.warn("Валидация не пройдена: дата релиза {} раньше 28.12.1895", film.getReleaseDate());
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года.");
        }

        if (film.getMpa() == null) {
            throw new ValidationException("Рейтинг MPA должен быть указан");
        }
        mpaStorage.findById(film.getMpa().getId()).orElseThrow(() ->
                new NotFoundException("Рейтинг с id = " + film.getMpa().getId() + " не найден"));

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Integer> requested = film.getGenres().stream()
                    .map(Genre::getId)
                    .collect(Collectors.toSet());
            Set<Integer> missing = new HashSet<>(requested);
            missing.removeAll(genreStorage.findExistingIds(requested));
            if (!missing.isEmpty()) {
                throw new NotFoundException("Жанр с id = " + missing.iterator().next() + " не найден");
            }
        }

        if (film.getDirectors() != null && !film.getDirectors().isEmpty()) {
            Set<Integer> requested = film.getDirectors().stream()
                    .map(Director::getId)
                    .collect(Collectors.toSet());
            Set<Integer> missing = new HashSet<>(requested);
            missing.removeAll(directorStorage.findExistingIds(requested));
            if (!missing.isEmpty()) {
                throw new NotFoundException("Режиссёр с id = " + missing.iterator().next() + " не найден");
            }
        }

        log.info("Валидация фильма пройдена успешно");
    }

    public Collection<Film> getPopularFilmsByDirectorId(int id, String sortBy) {
        log.info("Получен запрос GET /films/director/{}?sortBy={}", id, sortBy);
        directorStorage.findById(id).orElseThrow(() -> new NotFoundException("Режиссёр с id = " + id + " не найден"));
        if (!"year".equals(sortBy) && !"likes".equals(sortBy)) {
            throw new ValidationException("Недопустимое значение sortBy: " + sortBy);
        }
        return storage.getPopularFilmsByDirectorId(id, sortBy);
    }

    public Collection<Film> getCommonFilms(long userId, long friendId) {
        log.info("Получен запрос GET /films/common?userId={}&friendId={}", userId, friendId);
        userService.getUser(userId);
        userService.getUser(friendId);
        return storage.getCommonFilms(userId, friendId);
    }

    public Collection<Film> getRecommendations(long userId) {
        log.info("Получен запрос GET /users/{}/recommendations", userId);
        userService.getUser(userId);
        return storage.getRecommendations(userId);
    }
}
