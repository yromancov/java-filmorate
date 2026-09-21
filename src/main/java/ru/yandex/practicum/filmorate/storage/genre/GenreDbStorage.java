package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.BaseStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.*;

@Component
public class GenreDbStorage extends BaseStorage<Genre> implements GenreStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM genre ORDER BY genre_id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genre WHERE genre_id = ?";
    private static final String FIND_EXISTING_IDS_QUERY =
            "SELECT genre_id FROM genre WHERE genre_id IN (:ids)";


    public GenreDbStorage(NamedParameterJdbcTemplate namedJdbc, RowMapper<Genre> mapper) {
        super(namedJdbc, mapper);
    }

    public Collection<Genre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    public Optional<Genre> findById(int id) {
        return Optional.of(findOne(FIND_BY_ID_QUERY, id).orElseThrow(()
                -> new NotFoundException("Жанр с id = " + id + " не найден")));
    }

    // Данный метод полезен, когда в запросе передается массив из "id"
    // Основная логика:
    // - коллекцию "id" передаем в SQL запрос
    // - база смотрит что ей передали {1, 2, 999}
    // - выбирает только те что у нее есть {1, 2}
    // - возвращает Set {1, 2}
    public Set<Integer> findExistingIds(Collection<Integer> ids) {
        return new HashSet<>(namedJdbc.queryForList(FIND_EXISTING_IDS_QUERY,
                Map.of("ids", ids), Integer.class));
    }
}