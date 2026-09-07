package ru.yandex.practicum.filmorate.controller.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {
    Collection<User> findAll();

    User add(User user);

    User update(User newUser);

    long getNextId();

    User getUser(long id);

    Collection<User> getFriends(long id);

}
