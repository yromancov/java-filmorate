package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;

@Service
@Slf4j
public class DirectorService {
    private final DirectorStorage storage;

    public DirectorService(DirectorStorage storage){
        this.storage = storage;
    }
    public Collection<Director> findAll(){
        log.info("Получен запрос GET /directors");
        return storage.findAll();
    }

    public Director add(Director director){
        log.info("Получен запрос POST /directors");
        Director saved = storage.add(director);
        log.info("Пользователь с id={} успешно добавлен", saved.getId());
        return saved;
    }
    public Director findById(long id){

    }
}
