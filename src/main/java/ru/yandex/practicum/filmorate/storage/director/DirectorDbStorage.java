package ru.yandex.practicum.filmorate.storage.director;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.BaseStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.util.*;
@Component
public class DirectorDbStorage extends BaseStorage<Director> implements DirectorStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM directors ORDER BY director_id";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM directors WHERE director_id = ?";
    private static final String FIND_EXISTING_IDS_QUERY =
            "SELECT director_id FROM directors WHERE director_id IN (:ids)";
    private static final String INSERT_QUERY =
            "INSERT INTO directors(name) VALUES (?)";
    private static final String UPDATE_QUERY =
            "UPDATE users SET name = ?, WHERE director_id = ?";
    private static final String DELETE_QUERY =
            "DELETE from directors WHERE director_id = ?";

    // Что это такое?
    // Обёртка над JdbcTemplate
    // С его помощью в SQL запрос вместо одного параметра через {WHERE director_id = ?}
    // можно передать несколько значений через {WHERE director_id IN (:ids) где ids наша коллекция id}
    private final NamedParameterJdbcTemplate namedJdbc;

    public DirectorDbStorage(JdbcTemplate jdbc, RowMapper<Director> mapper) {
        super(jdbc, mapper);
        this.namedJdbc = new NamedParameterJdbcTemplate(jdbc);
    }

    @Override
    public Collection<Director> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Director> findById(int id) {
        return Optional.of(findOne(FIND_BY_ID_QUERY, id).orElseThrow(()
                -> new NotFoundException("Директор с id = " + id + " не найден")));
    }

    @Override
    public Set<Integer> findExistingIds(Collection<Integer> ids) {
        return new HashSet<>(namedJdbc.queryForList(
                FIND_EXISTING_IDS_QUERY, Map.of("ids", ids), Integer.class)
        );
    }

    @Override
    public Director add(Director director){
        int id = Math.toIntExact(insert(
                INSERT_QUERY,
                director.getName()
        ));
        director.setId(id);
        return director;
    }

    @Override
    public Director update(Director newDirector) {
        super.update(
                UPDATE_QUERY,
                newDirector.getName(),
                newDirector.getId()
        );
        return newDirector;
    }

    @Override
    public boolean deleteDirector(int id) {
        return super.delete(
                DELETE_QUERY,
                id
                );
    }

}
