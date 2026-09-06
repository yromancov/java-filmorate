package ru.yandex.practicum.filmorate.controller.storage.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.BaseStorage;
import ru.yandex.practicum.filmorate.model.StatusFriend;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component("userDbStorage")
public class UserDbStorage extends BaseStorage<User> implements UserStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_ALL_FRIENDS = "SELECT u.* FROM users u " +
            "JOIN follows f ON u.user_id = f.followed_user_id " +
            "WHERE f.following_user_id = ?";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = ?";
    private static final String INSERT_QUERY =
            "INSERT INTO users(name, login, email, birthday) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_QUERY =
            "UPDATE users SET name = ?, login = ?, email = ?, birthday = ? WHERE user_id = ?";

    private static final String INSERT_FRIEND_QUERY =
            "INSERT INTO follows(following_user_id, followed_user_id, status) VALUES (?, ?, ?)";

    private static final String EXISTS_REVERSE_QUERY =
            "SELECT COUNT(*) FROM follows WHERE following_user_id = ? AND followed_user_id = ?";

    private static final String UPDATE_STATUS_QUERY =
            "UPDATE follows SET status = ? WHERE following_user_id = ? AND followed_user_id = ?";

    private static final String DELETE_FRIEND_QUERY =
            "DELETE FROM follows WHERE following_user_id = ? AND followed_user_id = ?";

    private static final String FIND_COMMON_FRIENDS_QUERY =
            "SELECT u.* FROM users u " +
                    "JOIN follows f1 ON u.user_id = f1.followed_user_id " +
                    "JOIN follows f2 ON u.user_id = f2.followed_user_id " +
                    "WHERE f1.following_user_id = ? AND f2.following_user_id = ?";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<User> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public User add(User user) {
        long id = insert(
                INSERT_QUERY,
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                java.sql.Date.valueOf(user.getBirthday())
        );
        user.setId(id);
        return user;
    }

    @Override
    public User update(User newUser) {
        super.update(
                UPDATE_QUERY,
                newUser.getName(),
                newUser.getLogin(),
                newUser.getEmail(),
                java.sql.Date.valueOf(newUser.getBirthday()),
                newUser.getId()
        );
        return newUser;
    }

    @Override
    public Optional<User> getUser(long id) {
        return findOne(FIND_BY_ID_QUERY,id);
    }

    @Override
    public Collection<User> getFriends(long id) {
        return findMany(FIND_ALL_FRIENDS,id);
    }

    @Override
    public void addFriend(long id, long friendId) {
        if (hasRow(id, friendId)) {
            return;
        }
        boolean mutual = hasRow(friendId, id);

        jdbc.update(INSERT_FRIEND_QUERY, id, friendId,
                mutual ? StatusFriend.CONFIRM.name() : StatusFriend.NOT_CONFIRM.name());

        if (mutual) {
            jdbc.update(UPDATE_STATUS_QUERY, StatusFriend.CONFIRM.name(), friendId, id);
        }
    }

    @Override
    public void deleteFriend(long id, long friendId) {
        jdbc.update(DELETE_FRIEND_QUERY, id, friendId);
        jdbc.update(UPDATE_STATUS_QUERY, StatusFriend.NOT_CONFIRM.name(), friendId, id);
    }

    private boolean hasRow(long from, long to) {
        Integer count = jdbc.queryForObject(EXISTS_REVERSE_QUERY, Integer.class, from, to);
        return count != null && count > 0;
    }

    @Override
    public Collection<User> getCommonFriends(long id, long otherId) {
        return findMany(FIND_COMMON_FRIENDS_QUERY, id, otherId);
    }
}
