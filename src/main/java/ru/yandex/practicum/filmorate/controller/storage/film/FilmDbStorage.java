package ru.yandex.practicum.filmorate.controller.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.BaseStorage;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.Date;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

@Component("filmDbStorage")
public class FilmDbStorage extends BaseStorage<Film> implements FilmStorage {
    private static final String SELECT_BASE =
            "SELECT f.*, m.name AS mpa_name " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.age_rating_id = m.age_rating_id ";

    private static final String FIND_ALL_QUERY = SELECT_BASE;

    private static final String FIND_BY_ID_QUERY = SELECT_BASE + "WHERE f.film_id = ?";

    private static final String INSERT_QUERY =
            "INSERT INTO films(title, description, releaseDate, duration, age_rating_id) " +
                    "VALUES (?, ?, ?, ?, ?)";

    private static final String UPDATE_QUERY =
            "UPDATE films SET title = ?, description = ?, releaseDate = ?, duration = ?, age_rating_id = ? " +
                    "WHERE film_id = ?";

    private static final String DELETE_GENRES_QUERY =
            "DELETE FROM film_genre WHERE film_id = ?";

    private static final String INSERT_GENRE_QUERY =
            "INSERT INTO film_genre(film_id, genre_id) VALUES (?, ?)";

    private static final String FIND_GENRES_QUERY =
            "SELECT g.* FROM genre g " +
                    "JOIN film_genre fg ON g.genre_id = fg.genre_id " +
                    "WHERE fg.film_id = ? ORDER BY g.genre_id";

    private static final String FIND_LIKES_QUERY =
            "SELECT user_id FROM likesfilms WHERE film_id = ?";

    private static final String INSERT_LIKE_QUERY =
            "INSERT INTO likesfilms(film_id, user_id) VALUES (?, ?)";

    private static final String DELETE_LIKE_QUERY =
            "DELETE FROM likesfilms WHERE film_id = ? AND user_id = ?";

    private static final String FIND_POPULAR_QUERY =
            "SELECT f.*, m.name AS mpa_name, COUNT(l.user_id) AS likes_count " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.age_rating_id = m.age_rating_id " +
                    "LEFT JOIN likesfilms l ON f.film_id = l.film_id " +
                    "GROUP BY f.film_id, f.title, f.description, f.releaseDate, f.duration, f.age_rating_id, m.name " +
                    "ORDER BY likes_count DESC " +
                    "LIMIT ?";
    private static final String EXISTS_LIKE_QUERY =
            "SELECT COUNT(*) FROM likesfilms WHERE film_id = ? AND user_id = ?";


    private final RowMapper<Genre> genreMapper;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, RowMapper<Genre> genreMapper) {
        super(jdbc, mapper);
        this.genreMapper = genreMapper;
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = findMany(FIND_ALL_QUERY);
        films.forEach(this::loadGenres);
        return films;
    }

    @Override
    public Film add(Film film) {
        long id = insert(
                INSERT_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId()
        );
        film.setId(id);
        saveGenres(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        super.update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        jdbc.update(DELETE_GENRES_QUERY, film.getId());
        saveGenres(film);
        return film;
    }

    @Override
    public Optional<Film> getFilm(long id) {
        Optional<Film> film = findOne(FIND_BY_ID_QUERY, id);
        film.ifPresent(this::loadGenres);
        return film;
    }

    @Override
    public Collection<Film> getPopular(long count) {
        List<Film> films = findMany(FIND_POPULAR_QUERY, count);
        films.forEach(this::loadGenres);
        return films;
    }

    @Override
    public void addLike(long filmId, long userId) {
        Integer count = jdbc.queryForObject(EXISTS_LIKE_QUERY, Integer.class, filmId, userId);
        if (count != null && count > 0) {
            return;
        }
        jdbc.update(INSERT_LIKE_QUERY, filmId, userId);
    }

    @Override
    public void deleteLike(long filmId, long userId) {
        jdbc.update(DELETE_LIKE_QUERY, filmId, userId);
    }

    private void loadGenres(Film film) {
        film.setGenres(new LinkedHashSet<>(jdbc.query(FIND_GENRES_QUERY, genreMapper, film.getId())));
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null) {
            return;
        }
        for (Genre genre : film.getGenres()) {
            jdbc.update(INSERT_GENRE_QUERY, film.getId(), genre.getId());
        }
    }
}
