package ru.yandex.practicum.filmorate.storage.feed;

import ru.yandex.practicum.filmorate.model.Event;

import java.util.List;

public interface FeedStorage {

    void save(Event event);

    List<Event> findByUserId(Long userId);
}
