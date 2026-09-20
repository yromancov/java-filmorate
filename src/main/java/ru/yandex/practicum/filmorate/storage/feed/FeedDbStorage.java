package ru.yandex.practicum.filmorate.storage.feed;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dal.BaseStorage;
import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

@Slf4j
@Component
@Transactional
public class FeedDbStorage extends BaseStorage<Event> implements FeedStorage {

    private static final String INSERT_QUERY =
            "INSERT INTO feed(event_timestamp, user_id, event_type, operation, entity_id) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String FIND_BY_USER_ID_QUERY = "SELECT event_id, event_timestamp, user_id, event_type, " +
            "operation, entity_id " +
            "FROM feed WHERE user_id = ? ORDER BY event_timestamp ASC";

    public FeedDbStorage(JdbcTemplate jdbc, RowMapper<Event> rowMapper) {
        super(jdbc, rowMapper);
    }

    @Override
    public void save(Event event) {
        insert(INSERT_QUERY,
                event.getTimestamp(),
                event.getUserId(),
                event.getEventType().name(),
                event.getOperation().name(),
                event.getEntityId());
        log.debug("Сохранено событие: user={}, type={}, operation={}, entity={}",
                event.getUserId(), event.getEventType(),
                event.getOperation(), event.getEntityId());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findByUserId(Long userId) {
        log.debug("Поиск событий пользователя id={}", userId);
        return findMany(FIND_BY_USER_ID_QUERY, userId);
    }
}

