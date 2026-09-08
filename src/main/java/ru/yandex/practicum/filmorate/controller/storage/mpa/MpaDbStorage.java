package ru.yandex.practicum.filmorate.controller.storage.mpa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.BaseStorage;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

@Component
public class MpaDbStorage extends BaseStorage<Mpa> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM mpa ORDER BY age_rating_id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM mpa WHERE age_rating_id = ?";

    public MpaDbStorage(JdbcTemplate jdbc, RowMapper<Mpa> mapper) {
        super(jdbc, mapper);
    }

    public Collection<Mpa> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Mpa> findById(int id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }
}