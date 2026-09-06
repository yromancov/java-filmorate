package ru.yandex.practicum.filmorate.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.AgeRating;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class MpaRowMap implements RowMapper<AgeRating> {
    @Override
    public AgeRating mapRow(ResultSet rs, int rowNum) throws SQLException {
        AgeRating mpa = new AgeRating();
        mpa.setId(rs.getInt("age_rating_id"));
        mpa.setName(rs.getString("name"));
        return mpa;
    }
}