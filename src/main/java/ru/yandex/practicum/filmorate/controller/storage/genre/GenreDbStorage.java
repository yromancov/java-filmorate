package ru.yandex.practicum.filmorate.controller.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.BaseStorage;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

@Component
public class GenreDbStorage extends BaseStorage<Genre> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM genre ORDER BY genre_id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genre WHERE genre_id = ?";

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    public Collection<Genre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Genre> findById(int id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }
}