package ru.yandex.practicum.filmorate.controller.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User add(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User newUser) {
        getUser(newUser.getId());
        users.put(newUser.getId(), newUser);
        log.info("Пользователь с ID {} успешно обновлен ", newUser.getId());
        return newUser;
    }

    @Override
    public User getUser(long id) {
        if (!users.containsKey(id)) {
            log.warn("Пользователь с ID {} не найден ", id);
            throw new NotFoundException("Пользователь с ID " + id + " не найден.");
        }
        log.info("Пользователь с ID {} найден ", id);
        return users.get(id);
    }


    @Override
    public Collection<User> getFriends(long id) {
        User user = getUser(id);
        Set<Long> friendsId = user.getFriends();
        if (friendsId == null) {
            return List.of();
        }
        return friendsId.stream()
                .map(this::getUser)
                .filter(Objects::nonNull)
                .toList();

    }


    @Override
    public long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}
