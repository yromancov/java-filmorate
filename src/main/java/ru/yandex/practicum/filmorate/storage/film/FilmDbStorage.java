package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.dal.BaseStorage;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.stream.Collectors;

@Component
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

    private static final String INSERT_DIRECTOR_QUERY =
            "INSERT INTO film_director(film_id, director_id) VALUES (?, ?)";

    private static final String FIND_GENRES_QUERY =
            "SELECT g.* FROM genre g " +
                    "JOIN film_genre fg ON g.genre_id = fg.genre_id " +
                    "WHERE fg.film_id = ? ORDER BY g.genre_id";

    private static final String INSERT_LIKE_QUERY =
            "INSERT INTO likesfilms(film_id, user_id) VALUES (?, ?)";

    private static final String DELETE_LIKE_QUERY =
            "DELETE FROM likesfilms WHERE film_id = ? AND user_id = ?";

    private static final String FIND_GENRES_FOR_FILMS_QUERY =
            "SELECT fg.film_id, g.* FROM genre g " +
                    "JOIN film_genre fg ON g.genre_id = fg.genre_id " +
                    "WHERE fg.film_id IN (:ids) ORDER BY g.genre_id";


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
    private static final String DELETE_FILM_QUERY = "DELETE FROM films WHERE film_id = ?";

    private static final String FIND_DIRECTORS_QUERY =
            "SELECT d.* FROM directors d " +
                    "JOIN film_director fd ON d.director_id = fd.director_id " +
                    "WHERE fd.film_id = ? ORDER BY d.director_id";

    private static final String FIND_DIRECTORS_FOR_FILMS_QUERY =
            "SELECT fd.film_id, d.* FROM directors d " +
                    "JOIN film_director fd ON d.director_id = fd.director_id " +
                    "WHERE fd.film_id IN (:ids) ORDER BY d.director_id";

    private static final String DELETE_DIRECTORS_QUERY =
            "DELETE FROM film_director WHERE film_id = ?";

    private static final String FIND_BY_DIRECTOR_SORTED_BY_LIKES_QUERY =
            "SELECT f.*, m.name AS mpa_name, COUNT(l.user_id) AS likes_count " +
                    "FROM films f " +
                    "JOIN film_director fd ON f.film_id = fd.film_id " +
                    "LEFT JOIN mpa m ON f.age_rating_id = m.age_rating_id " +
                    "LEFT JOIN likesfilms l ON f.film_id = l.film_id " +
                    "WHERE fd.director_id = ? " +
                    "GROUP BY f.film_id, f.title, f.description, f.releaseDate, " +
                    "f.duration, f.age_rating_id, m.name " +
                    "ORDER BY likes_count DESC, f.film_id";

    private static final String FIND_BY_DIRECTOR_SORTED_BY_YEAR_QUERY =
            SELECT_BASE +
                    "JOIN film_director fd ON f.film_id = fd.film_id " +
                    "WHERE fd.director_id = ? " +
                    "ORDER BY f.releaseDate, f.film_id";

    private static final String FIND_COMMON_FILMS_QUERY =
            "SELECT f.*, m.name AS mpa_name, COUNT(l_all.user_id) AS likes_count " +
                    "FROM films f " +
                    "LEFT JOIN mpa m ON f.age_rating_id = m.age_rating_id " +
                    "JOIN likesfilms l1 ON f.film_id = l1.film_id AND l1.user_id = ? " +
                    "JOIN likesfilms l2 ON f.film_id = l2.film_id AND l2.user_id = ? " +
                    "LEFT JOIN likesfilms l_all ON f.film_id = l_all.film_id " +
                    "GROUP BY f.film_id, f.title, f.description, f.releaseDate, f.duration, f.age_rating_id, m.name " +
                    "ORDER BY likes_count DESC";

    private static final String FIND_FILMS_BY_TITLE =
            "SELECT f.*, m.name AS mpa_name, COUNT(DISTINCT l.user_id) AS likes_count " +
                    "FROM films f " +
                    "LEFT JOIN film_director fd ON f.film_id = fd.film_id " +
                    "LEFT JOIN mpa m ON f.age_rating_id = m.age_rating_id " +
                    "LEFT JOIN likesfilms l ON f.film_id = l.film_id " +
                    "LEFT JOIN directors d ON fd.director_id = d.director_id " +
                    "WHERE LOWER(f.title) LIKE ? " +
                    "GROUP BY f.film_id, f.title, f.description, f.releaseDate, " +
                    "f.duration, f.age_rating_id, m.name " +
                    "ORDER BY likes_count DESC, f.film_id";

    private static final String FIND_FILMS_BY_DIRECTOR =
            "SELECT f.*, m.name AS mpa_name, COUNT(DISTINCT l.user_id) AS likes_count " +
                    "FROM films f " +
                    "LEFT JOIN film_director fd ON f.film_id = fd.film_id " +
                    "LEFT JOIN mpa m ON f.age_rating_id = m.age_rating_id " +
                    "LEFT JOIN likesfilms l ON f.film_id = l.film_id " +
                    "LEFT JOIN directors d ON fd.director_id = d.director_id " +
                    "WHERE LOWER(d.name) LIKE ? " +
                    "GROUP BY f.film_id, f.title, f.description, f.releaseDate, " +
                    "f.duration, f.age_rating_id, m.name " +
                    "ORDER BY likes_count DESC, f.film_id";

    private static final String FIND_FILM_BY_TITLE_AND_DIRECTOR =
            "SELECT f.*, m.name AS mpa_name, COUNT(DISTINCT l.user_id) AS likes_count " +
                    "FROM films f " +
                    "LEFT JOIN film_director fd ON f.film_id = fd.film_id " +
                    "LEFT JOIN mpa m ON f.age_rating_id = m.age_rating_id " +
                    "LEFT JOIN likesfilms l ON f.film_id = l.film_id " +
                    "LEFT JOIN directors d ON fd.director_id = d.director_id " +
                    "WHERE LOWER(f.title) LIKE ? OR LOWER(d.name) LIKE ? " +
                    "GROUP BY f.film_id, f.title, f.description, f.releaseDate, " +
                    "f.duration, f.age_rating_id, m.name " +
                    "ORDER BY likes_count DESC, f.film_id";

    @Override
    public boolean delete(long id) {
        return super.delete(DELETE_FILM_QUERY, id);
    }

    private final RowMapper<Genre> genreMapper;
    private final RowMapper<Director> directorMapper;
    private final NamedParameterJdbcTemplate namedJdbc;

    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper, RowMapper<Genre> genreMapper, RowMapper<Director> directorMapper) {
        super(jdbc, mapper);
        this.genreMapper = genreMapper;
        this.directorMapper = directorMapper;
        this.namedJdbc = new NamedParameterJdbcTemplate(jdbc);
    }

    @Override
    public Collection<Film> getCommonFilms(long userId, long friendId) {
        List<Film> films = findMany(FIND_COMMON_FILMS_QUERY, userId, friendId);
        loadGenresForFilms(films);
        return films;
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = findMany(FIND_ALL_QUERY);
        loadGenresForFilms(films);
        loadDirectorsForFilms(films);
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
        saveDirectors(film);
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
        jdbc.update(DELETE_DIRECTORS_QUERY, film.getId());
        saveGenres(film);
        saveDirectors(film);
        return film;
    }

    @Override
    public Optional<Film> getFilm(long id) {
        Optional<Film> film = findOne(FIND_BY_ID_QUERY, id);
        film.ifPresent(this::loadGenres);
        film.ifPresent(this::loadDirectors);
        return film;
    }

    @Override
    public Collection<Film> getPopular(long count) {
        List<Film> films = findMany(FIND_POPULAR_QUERY, count);
        loadGenresForFilms(films);
        loadDirectorsForFilms(films);
        return films;
    }

    private void loadGenresForFilms(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }
        Map<Long, Film> byId = films.stream()
                .collect(Collectors.toMap(Film::getId, film -> film));

        namedJdbc.query(FIND_GENRES_FOR_FILMS_QUERY, Map.of("ids", byId.keySet()), rs -> {
            byId.get(rs.getLong("film_id")).getGenres().add(genreMapper.mapRow(rs, 0));
        });
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
        List<Genre> genres = List.copyOf(film.getGenres());
        jdbc.batchUpdate(INSERT_GENRE_QUERY,
                genres, genres.size(),
                (PreparedStatement ps, Genre genre) -> {
                    ps.setLong(1, film.getId());
                    ps.setInt(2, genre.getId());
                }
        );
    }

    private void loadDirectors(Film film) {
        film.setDirectors(new LinkedHashSet<>(jdbc.query(FIND_DIRECTORS_QUERY, directorMapper, film.getId())));
    }

    private void saveDirectors(Film film) {
        if (film.getDirectors() == null) {
            return;
        }
        List<Director> directors = List.copyOf(film.getDirectors());
        jdbc.batchUpdate(INSERT_DIRECTOR_QUERY,
                directors, directors.size(),
                (PreparedStatement ps, Director director) -> {
                    ps.setLong(1, film.getId());
                    ps.setInt(2, director.getId());
                }
        );
    }

    private void loadDirectorsForFilms(List<Film> films) {
        if (films.isEmpty()) {
            return;
        }
        Map<Long, Film> byId = films.stream()
                .collect(Collectors.toMap(Film::getId, film -> film));

        namedJdbc.query(FIND_DIRECTORS_FOR_FILMS_QUERY, Map.of("ids", byId.keySet()), rs -> {
            byId.get(rs.getLong("film_id")).getDirectors().add(directorMapper.mapRow(rs, 0));
        });
    }

    public Collection<Film> getPopularFilmsByDirectorId(int id, String sortBy) {
        String query = "year".equals(sortBy) ?
                FIND_BY_DIRECTOR_SORTED_BY_YEAR_QUERY
                : FIND_BY_DIRECTOR_SORTED_BY_LIKES_QUERY;
        List<Film> films = findMany(query, id);
        loadGenresForFilms(films);
        loadDirectorsForFilms(films);
        return films;
    }

    public Collection<Film> searchByTitleOrDirector(String query, String by) {
        String pattern = "%" + query.toLowerCase() + "%";
        Set<String> parts = Arrays.stream(by.toLowerCase().split(","))
                .map(String::trim)
                .collect(Collectors.toSet());
        boolean byTitle = parts.contains("title");
        boolean byDirector = parts.contains("director");

        if (!byTitle && !byDirector) {
            throw new ValidationException("Параметр by должен содеражть title и/или director");
        }
        List<Film> films = List.of();
        if (byTitle && byDirector) {
            films = findMany(FIND_FILM_BY_TITLE_AND_DIRECTOR, pattern , pattern);
        } else if (byTitle) {
            films = findMany(FIND_FILMS_BY_TITLE, pattern);
        } else if (byDirector) {
            films = findMany(FIND_FILMS_BY_DIRECTOR, pattern);
        }
        loadGenresForFilms(films);
        loadDirectorsForFilms(films);
        return films;

    }

}
