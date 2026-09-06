package ru.yandex.practicum.filmorate.controller.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.StatusFriend;
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
        getUserOrThrow(newUser.getId());
        users.put(newUser.getId(), newUser);
        log.info("Пользователь с ID {} успешно обновлен ", newUser.getId());
        return newUser;
    }

    private User getUserOrThrow(long id) {
        return getUser(id).orElseThrow(() ->
                new NotFoundException("Пользователь с ID " + id + " не найден."));
    }

    @Override
    public Optional<User> getUser(long id) {
        User user = users.get(id);
        if (user == null) {
            log.warn("Пользователь с ID {} не найден ", id);
            return Optional.empty();
        }
        log.info("Пользователь с ID {} найден ", id);
        return Optional.of(user);
    }


    @Override
    public Collection<User> getFriends(long id) {
        User user = getUserOrThrow(id);
        Map<Long, StatusFriend> friendsMap = user.getFriends();
        if (friendsMap == null || friendsMap.isEmpty()) {
            return List.of();
        }
        return friendsMap.keySet().stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .toList();
    }

    @Override
    public void addFriend(long id, long friendId) {
        User user = getUserOrThrow(id);
        User friend = getUserOrThrow(friendId);

        if (friend.getFriends().containsKey(id)) {
            user.getFriends().put(friendId, StatusFriend.CONFIRM);
            friend.getFriends().put(id, StatusFriend.CONFIRM);
        } else {
            user.getFriends().put(friendId, StatusFriend.NOT_CONFIRM);
        }
    }

    @Override
    public void deleteFriend(long id, long friendId) {
        getUserOrThrow(id).getFriends().remove(friendId);
        getUserOrThrow(friendId).getFriends().remove(id);
    }

    @Override
    public Collection<User> getCommonFriends(long id, long otherId) {
        Set<Long> otherFriends = getUserOrThrow(otherId).getFriends().keySet();
        return getUserOrThrow(id).getFriends().keySet().stream()
                .filter(otherFriends::contains)
                .map(users::get)
                .filter(Objects::nonNull)
                .toList();
    }


    public long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }

}
