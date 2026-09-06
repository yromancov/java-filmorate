package ru.yandex.practicum.filmorate.mapper;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import org.springframework.jdbc.core.RowMapper;import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class UserRowMap implements RowMapper<User> {
    @Override
    public User mapRow(ResultSet resultSet, int rowNum) throws SQLException{
        User user = new User();
        user.setId(resultSet.getLong("user_id"));
        user.setName(resultSet.getString("name"));
        user.setLogin(resultSet.getString("login"));
        user.setEmail(resultSet.getString("email"));
        user.setBirthday(resultSet.getDate("birthday").toLocalDate());

        return user;
    }
}
