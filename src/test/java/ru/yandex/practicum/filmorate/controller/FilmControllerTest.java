package ru.yandex.practicum.filmorate.controller;

import static org.mockito.Mockito.*;

import org.mockito.Mockito;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.controller.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.controller.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class FilmControllerTest {
    private FilmService filmService;
    private Validator validator;
    private Film film;

    @BeforeEach
    void setUp() {
        FilmStorage filmStorage = Mockito.mock(FilmStorage.class);
        UserService userService = Mockito.mock(UserService.class);
        MpaDbStorage mpaStorage = Mockito.mock(MpaDbStorage.class);
        GenreDbStorage genreStorage = Mockito.mock(GenreDbStorage.class);

        when(mpaStorage.findById(anyInt())).thenReturn(Optional.of(new Mpa()));
        when(filmStorage.add(any(Film.class))).thenAnswer(inv -> inv.getArgument(0));

        filmService = new FilmService(filmStorage, userService, mpaStorage, genreStorage);

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        film = new Film();
        film.setName("The Odyseey");
        film.setDescription("Best film");
        film.setReleaseDate(LocalDate.of(2026, 7, 17));
        film.setDuration(180);
        Mpa mpa = new Mpa();
        mpa.setId(1);
        film.setMpa(mpa);
    }

    @Test
    void shouldRejectFilmWithBlankName() {
        film.setName(" ");

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Должна быть ошибка валидации для пустого имени");
    }

    @Test
    void shouldRejectFilmWithDescriptionLongerThan200Characters() {
        film.setDescription("a".repeat(201));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Должна быть ошибка валидации для описания > 200 символов");
    }

    @Test
    void shouldAcceptFilmWithDescriptionOf200Characters() {
        film.setDescription("a".repeat(200));

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertTrue(violations.isEmpty(), "Описание ровно в 200 символов должно быть валидным");
        assertDoesNotThrow(() -> filmService.add(film));
    }

    @Test
    void shouldRejectFilmReleasedBeforeDecember28th1895() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(ValidationException.class, () -> filmService.add(film));
    }

    @Test
    void shouldAcceptFilmReleasedOnDecember28th1895() {
        film.setReleaseDate(LocalDate.of(1895, 12, 28));

        assertDoesNotThrow(() -> filmService.add(film));
    }

    @Test
    void shouldRejectFilmWithZeroDuration() {
        film.setDuration(0);

        Set<ConstraintViolation<Film>> violations = validator.validate(film);
        assertFalse(violations.isEmpty(), "Должна быть ошибка валидации для нулевой длины");
    }
}

