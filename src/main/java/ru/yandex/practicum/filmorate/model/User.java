package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@EqualsAndHashCode(of = "id")
public class User {
    private Map<Long, StatusFriend> friends = new HashMap<>();
    private Long id;
    @NotBlank(message = "Почта не может быть пустой.")
    @Email(message = "Почта должна соответствовать формату email (содержать @).")
    private String email;
    @NotNull
    @NotBlank(message = "Логин не должен быть пустым.")
    private String login;
    private String name;
    @NotNull
    @PastOrPresent(message = "Дата рождения не может быть в будущем.")
    private LocalDate birthday;

}
