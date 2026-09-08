package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.controller.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.controller.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.mapper.GenreRowMap;
import ru.yandex.practicum.filmorate.mapper.MpaRowMap;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({MpaDbStorage.class, GenreDbStorage.class, MpaRowMap.class, GenreRowMap.class})
class DictionaryStoragesTests {

    private final MpaDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;

    @Test
    public void testFindAllMpa() {
        Collection<Mpa> mpaList = mpaStorage.findAll();
        assertThat(mpaList).hasSize(5);
    }

    @Test
    public void testFindMpaById() {
        Optional<Mpa> mpaOptional = mpaStorage.findById(4); // R в data.sql
        assertThat(mpaOptional)
                .isPresent()
                .hasValueSatisfying(mpa -> assertThat(mpa.getName()).isEqualTo("R"));
    }

    @Test
    public void testFindAllGenres() {
        Collection<Genre> genres = genreStorage.findAll();
        assertThat(genres).hasSize(6);
    }

    @Test
    public void testFindGenreById() {
        Optional<Genre> genreOptional = genreStorage.findById(6); // Боевик
        assertThat(genreOptional)
                .isPresent()
                .hasValueSatisfying(genre -> assertThat(genre.getName()).isEqualTo("Боевик"));
    }
}
