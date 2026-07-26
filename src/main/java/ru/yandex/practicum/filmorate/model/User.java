package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(of = "id")
public class User {
    private Long id;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(regexp = "\\S+")
    private String login;
    private String name;

    @NotNull
    @PastOrPresent
    private LocalDate birthday;
}
