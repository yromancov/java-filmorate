package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;

@Service
@Slf4j
public class DirectorService {
    private final DirectorStorage storage;

    public DirectorService(DirectorStorage storage) {
        this.storage = storage;
    }

    public Collection<Director> findAll() {
        log.info("Получен запрос GET /directors");
        return storage.findAll();
    }

    public Director add(Director director) {
        log.info("Получен запрос POST /directors");
        Director saved = storage.add(director);
        log.info("Пользователь с id={} успешно добавлен", saved.getId());
        return saved;
    }

    public Director findById(long id) {
        return storage.findById((int) id).orElseThrow(() ->
                new NotFoundException("Режиссер с Id " + id + " не найден."));

    }
    public Director update(Director newDirector){
        log.info("Получен запрос PUT /dorectors");
        if (newDirector.getId() == 0){
            log.warn("Обновление невозможно: Id режиccера не указан");
            throw new ValidationException("Id должен быть указан");
        }
        findById(newDirector.getId());
        Director updated = storage.update(newDirector);
        log.info("Режиссер с id={} успешно обновлён", updated.getId());
        return updated;
    }

    public void deleteDirector(int id){
        log.info("Получен запрос DELETE /directors/{}",id);
        if (!storage.deleteDirector(id)){
            throw new NotFoundException("Режиссёр с id = " + id + " не найден");

        }
        log.info("Режиссёр с id ={} удален", id);
    }

}
