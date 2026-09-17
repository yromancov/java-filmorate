package ru.yandex.practicum.filmorate.storage.director;

import ru.yandex.practicum.filmorate.model.Director;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface DirectorStorage {
    Collection<Director> findAll();

    Optional<Director> findById(int id);

    //Метод для поиска несуществующих "id" переданным пользователем
    Set<Integer> findExistingIds(Collection<Integer> ids);

    Director add(Director director);

    Director update(Director newDirector);

    boolean deleteDirectorById(int id);
}
