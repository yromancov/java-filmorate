package ru.yandex.practicum.filmorate.controller.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.BaseStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

@Component
public class GenreDbStorage extends BaseStorage<Genre> {
    private static final String FIND_ALL_QUERY = "SELECT * FROM genre ORDER BY genre_id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genre WHERE genre_id = ?";
    private static final String FIND_EXISTING_IDS_QUERY =
            "SELECT genre_id FROM genre WHERE genre_id IN (:ids)";

    private final NamedParameterJdbcTemplate namedJdbc;

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
        this.namedJdbc = new NamedParameterJdbcTemplate(jdbc);
    }

    public Collection<Genre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Genre> findById(int id) {
        return Optional.of(findOne(FIND_BY_ID_QUERY, id).orElseThrow(() -> new NotFoundException("Жанр с id = " + id + " не найден")));
    }

    public Set<Integer> findExistingIds(Collection<Integer> ids) {
        return new HashSet<>(namedJdbc.queryForList(FIND_EXISTING_IDS_QUERY,
                Map.of("ids", ids), Integer.class));
    }
}