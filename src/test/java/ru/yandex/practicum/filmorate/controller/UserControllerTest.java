package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserControllerTest {
    private UserController userController;
    private User user;

    @BeforeEach
    void setUp() {
        userController = new UserController();

        user = new User();
        user.setEmail("john@example.com");
        user.setLogin("john");
        user.setName("John Doe");
        user.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void shouldRejectUserWithBlankEmail() {
        user.setEmail(" ");

        assertThrows(ValidationException.class, () -> userController.add(user));
    }

    @Test
    void shouldRejectUserWithEmailWithoutAtSign() {
        user.setEmail("john.example.com");

        assertThrows(ValidationException.class, () -> userController.add(user));
    }

    @Test
    void shouldRejectUserWithBlankLogin() {
        user.setLogin(" ");

        assertThrows(ValidationException.class, () -> userController.add(user));
    }

    @Test
    void shouldRejectUserWithLoginContainingSpaces() {
        user.setLogin("john doe");

        assertThrows(ValidationException.class, () -> userController.add(user));
    }

    @Test
    void shouldSetLoginAsNameWhenNameIsBlank() {
        user.setName(" ");

        User createdUser = userController.add(user);

        assertEquals(createdUser.getLogin(), createdUser.getName());
    }

    @Test
    void shouldRejectUserBornInFuture() {
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userController.add(user));
    }

    @Test
    void shouldAcceptUserBornToday() {
        user.setBirthday(LocalDate.now());

        assertDoesNotThrow(() -> userController.add(user));
    }
}