package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.storage.user.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserService userService;
    private Validator validator;
    private User user;

    @BeforeEach
    void setUp() {
        userService = new UserService(new InMemoryUserStorage());

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        user = new User();
        user.setEmail("john@example.com");
        user.setLogin("john");
        user.setName("John Doe");
        user.setBirthday(LocalDate.of(1990, 1, 1));
    }

    @Test
    void shouldRejectUserWithBlankEmail() {
        user.setEmail(" ");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Должна быть ошибка валидации для пустого email");
    }

    @Test
    void shouldRejectUserWithEmailWithoutAtSign() {
        user.setEmail("john.example.com");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Аннотация @Email должна отклонить адрес без символа @");
    }

    @Test
    void shouldRejectUserWithBlankLogin() {
        user.setLogin(" ");

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Должна быть ошибка валидации для пустого логина");
    }

    @Test
    void shouldRejectUserWithLoginContainingSpaces() {
        user.setLogin("john doe");

        assertThrows(ValidationException.class, () -> userService.add(user));
    }

    @Test
    void shouldSetLoginAsNameWhenNameIsBlank() {
        user.setName(" ");

        User createdUser = userService.add(user);

        assertEquals(createdUser.getLogin(), createdUser.getName());
    }

    @Test
    void shouldRejectUserBornInFuture() {
        user.setBirthday(LocalDate.now().plusDays(1));

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty(), "Аннотация @PastOrPresent должна отклонить дату в будущем");
    }

    @Test
    void shouldAcceptUserBornToday() {
        user.setBirthday(LocalDate.now());

        assertDoesNotThrow(() -> userService.add(user));
    }
}