package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.Duration;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilmControllerTest {
    private FilmController filmController;
    private Film film;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();

        film = new Film();
        film.setName("The Odyseey");
        film.setDescription("Best film");
        film.setReleaseDate(LocalDate.of(2026, 7, 17));
        film.setDuration(180);
    }

    @Test
    void shouldRejectFilmWithBlankName() {
        film.setName(" ");

        assertThrows(ValidationException.class, () -> filmController.add(film));
    }

    @Test
    void shouldRejectFilmWithDescriptionLongerThan200Characters() {
        film.setDescription("a".repeat(201));

        assertThrows(ValidationException.class, () -> filmController.add(film));
    }

    @Test
    void shouldAcceptFilmWithDescriptionOf200Characters() {
        film.setDescription("a".repeat(200));

        assertDoesNotThrow(() -> filmController.add(film));
    }

    @Test
    void shouldRejectFilmReleasedBeforeDecember28th1895() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));

        assertThrows(ValidationException.class, () -> filmController.add(film));
    }

    @Test
    void shouldAcceptFilmReleasedOnDecember28th1895() {
        film.setReleaseDate(LocalDate.of(1895, 12, 28));

        assertDoesNotThrow(() -> filmController.add(film));
    }

    @Test
    void shouldRejectFilmWithZeroDuration() {
        film.setDuration(0);

        assertThrows(ValidationException.class, () -> filmController.add(film));
    }
}

