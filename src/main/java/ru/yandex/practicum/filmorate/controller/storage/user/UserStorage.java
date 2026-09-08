package ru.yandex.practicum.filmorate.controller.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {
    Collection<User> findAll();

    User add(User user);

    User update(User newUser);

    Optional<User> getUser(long id);

    Collection<User> getFriends(long id);

    void addFriend(long id, long friendId);

    void deleteFriend(long id, long friendId);

    Collection<User> getCommonFriends(long id, long otherId);

}
