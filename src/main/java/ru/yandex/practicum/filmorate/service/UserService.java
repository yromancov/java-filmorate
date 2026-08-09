package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.controller.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.exception.DuplicateUserException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class UserService {

    private final UserStorage storage;

    public UserService(UserStorage storage) {
        this.storage = storage;
    }

    public Collection<User> findAll() {
        log.info("Получен запрос GET /users");
        return storage.findAll();
    }

    public User add(User user) {
        log.info("Получен запрос POST /users");
        validateUser(user);
        checkEmailNotTaken(user.getEmail(), null);
        User saved = storage.add(user);
        log.info("Пользователь с id={} успешно добавлен", saved.getId());
        return saved;
    }

    public User getUser(long id) {
        return storage.getUser(id);
    }

    public void addFriend(long id, long friendId) {
        User user = storage.getUser(id);
        User friend = storage.getUser(friendId);

        user.getFriends().add(friendId);
        friend.getFriends().add(id);

        storage.update(user);
        storage.update(friend);


    }

    public Collection<User> getFriends(long id) {
        return storage.getFriends(id);
    }

    public void deleteFriendById(long id, long friendId) {
        User user = storage.getUser(id);
        User friend = storage.getUser(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);

        storage.update(user);
        storage.update(friend);

    }

    public Collection<User> getCommonFriends(long id, long otherId) {
        User user = storage.getUser(id);
        User otherUser = storage.getUser(otherId);

        Set<Long> userFriends = user.getFriends();
        Set<Long> otherUserFriends = otherUser.getFriends();

        if (userFriends == null || userFriends.isEmpty() || otherUserFriends == null || otherUserFriends.isEmpty()) {
            return List.of();
        }
        return userFriends.stream()
                .filter(otherUserFriends::contains)
                .map(storage::getUser)
                .toList();

    }


    public User update(User newUser) {
        log.info("Получен запрос PUT /users");
        if (newUser.getId() == null) {
            log.warn("Обновление невозможно: id пользователя не указан");
            throw new ValidationException("Id должен быть указан");
        }
        validateUser(newUser);
        checkEmailNotTaken(newUser.getEmail(), newUser.getId());
        User updated = storage.update(newUser);
        log.info("Пользователь с id={} успешно обновлён", updated.getId());
        return updated;
    }

    private void checkEmailNotTaken(String email, Long excludeUserId) {
        String normalized = email.toLowerCase();
        boolean taken = storage.findAll().stream()
                .filter(u -> excludeUserId == null || !u.getId().equals(excludeUserId))
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(normalized));
        if (taken) {
            log.warn("Обнаружен пользователь с уже существующим email {}", email);
            throw new DuplicateUserException("Пользователь с таким email уже существует.");
        }
    }


    public void validateUser(User user) {
        log.info("Запуск валидации");

        if (user.getLogin().contains(" ")) {
            log.warn("Валидация не пройдена: Логин не должен быть пустым и содержать пробелы");
            throw new ValidationException("Логин не должен быть пустым и содержать пробелы.");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            log.info("У пользователя отсутствует имя, оно заменено логином {}", user.getLogin());
            user.setName(user.getLogin());
        }

        log.info("Валидация пройдена успешно!");

    }

}
