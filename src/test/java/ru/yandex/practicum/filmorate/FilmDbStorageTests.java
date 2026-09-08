package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.controller.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.controller.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.mapper.FilmRowMap;
import ru.yandex.practicum.filmorate.mapper.GenreRowMap;
import ru.yandex.practicum.filmorate.mapper.MpaRowMap;
import ru.yandex.practicum.filmorate.mapper.UserRowMap;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({
        FilmDbStorage.class,
        UserDbStorage.class,
        FilmRowMap.class,
        UserRowMap.class,
        GenreRowMap.class,
        MpaRowMap.class
})
class FilmDbStorageTests {

    private final FilmDbStorage filmStorage;

    @Test
    public void testGetFilm_ShouldReturnMatrixFromDataSql() {
        Optional<Film> filmOptional = filmStorage.getFilm(1L);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film).hasFieldOrPropertyWithValue("id", 1L);
                    assertThat(film).hasFieldOrPropertyWithValue("name", "Матрица");
                    assertThat(film).hasFieldOrPropertyWithValue("duration", 136);
                    assertThat(film.getMpa().getId()).isEqualTo(4); // R
                });
    }

    @Test
    public void testFindAll_ShouldReturnInitialFilms() {
        Collection<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(2);
    }

    @Test
    public void testAdd_ShouldCreateNewFilm() {
        Film film = new Film();
        film.setName("Интерстеллар");
        film.setDescription("Научная фантастика Нолана");
        film.setReleaseDate(LocalDate.of(2014, 11, 6));
        film.setDuration(169);
        Mpa mpa = new Mpa();
        mpa.setId(3); // PG-13
        film.setMpa(mpa);

        Film savedFilm = filmStorage.add(film);

        assertThat(savedFilm).isNotNull();
        assertThat(savedFilm.getId()).isNotNull();

        Optional<Film> filmOptional = filmStorage.getFilm(savedFilm.getId());
        assertThat(filmOptional).isPresent();
    }

    @Test
    public void testUpdate_ShouldModifyFilm() {
        Film filmToUpdate = new Film();
        filmToUpdate.setId(2L); // Изменяем 'Король Лев'
        filmToUpdate.setName("Король Лев 3D");
        filmToUpdate.setDescription("Обновленная история Симбы");
        filmToUpdate.setReleaseDate(LocalDate.of(1994, 6, 24));
        filmToUpdate.setDuration(90);
        Mpa mpa = new Mpa();
        mpa.setId(1);
        filmToUpdate.setMpa(mpa);

        filmStorage.update(filmToUpdate);

        Optional<Film> filmOptional = filmStorage.getFilm(2L);
        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film -> {
                    assertThat(film).hasFieldOrPropertyWithValue("name", "Король Лев 3D");
                    assertThat(film).hasFieldOrPropertyWithValue("duration", 90);
                });
    }

    @Test
    public void testGetPopular_ShouldReturnFilmsOrderedByLikes() {
        Collection<Film> popular = filmStorage.getPopular(10);

        assertThat(popular).hasSize(2);
        Film firstPopular = popular.iterator().next();
        assertThat(firstPopular.getId()).isEqualTo(1L); // Матрица популярнее
    }

    @Test
    public void testAddAndDeleteLike() {
        filmStorage.addLike(2L, 2L);

        filmStorage.deleteLike(2L, 1L);
    }
}
