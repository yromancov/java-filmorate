package ru.yandex.practicum.filmorate.storage.review;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.dal.BaseStorage;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@Transactional
public class ReviewDbStorage extends BaseStorage<Review> implements ReviewStorage {

    private static final String BASE_SELECT =
            "SELECT r.review_id, r.content, r.is_positive, r.user_id, r.film_id, r.useful " +
                    "FROM reviews r ";

    private static final String FIND_BY_ID_QUERY =
            BASE_SELECT + "WHERE r.review_id = ?";

    private static final String FIND_BY_FILM_QUERY =
            BASE_SELECT + "WHERE r.film_id = ? " +
                    "ORDER BY r.useful DESC, r.review_id ASC LIMIT ?";

    private static final String FIND_ALL_QUERY =
            BASE_SELECT + "ORDER BY r.useful DESC, r.review_id ASC LIMIT ?";

    private static final String INSERT_QUERY =
            "INSERT INTO reviews(content, is_positive, user_id, film_id) VALUES (?, ?, ?, ?)";

    private static final String UPDATE_QUERY =
            "UPDATE reviews SET content = ?, is_positive = ? WHERE review_id = ?";

    private static final String DELETE_QUERY =
            "DELETE FROM reviews WHERE review_id = ?";

    private static final String INSERT_LIKE_QUERY =
            "INSERT INTO review_likes(review_id, user_id, is_useful) VALUES (?, ?, ?)";

    private static final String DELETE_LIKE_QUERY =
            "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";

    private static final String UPDATE_USEFUL_QUERY =
            "UPDATE reviews SET useful = useful + ? WHERE review_id = ?";

    private static final String FIND_LIKE_QUERY =
            "SELECT is_useful FROM review_likes WHERE review_id = ? AND user_id = ?";

    private static final String EXISTS_REVIEW_QUERY =
            "SELECT COUNT(*) FROM reviews WHERE user_id = ? AND film_id = ?";

    private static final String EXISTS_LIKE_QUERY =
            "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?";

    public ReviewDbStorage(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        super(jdbc, mapper);
    }


    @Override
    public Review add(Review review) {
        long id = insert(INSERT_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getUserId(),
                review.getFilmId());
        review.setReviewId(id);
        review.setUseful(0);
        log.debug("Создан отзыв id={} на фильм id={} от пользователя id={}",
                id, review.getFilmId(), review.getUserId());
        return review;
    }

    @Override
    public Review update(Review review) {
        update(UPDATE_QUERY,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId());
        log.debug("Обновлён отзыв id={}", review.getReviewId());
        return findById(review.getReviewId())
                .orElseThrow(() -> new NotFoundException("Отзыв не найден после обновления"));
    }

    @Override
    public void delete(Long id) {
        jdbc.update(DELETE_QUERY, id);
        log.debug("Удалён отзыв id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Review> findById(Long id) {
        log.debug("Поиск отзыва по id={}", id);
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> findByFilmId(Long filmId, int count) {
        log.debug("Поиск отзывов по фильму id={}, count={}", filmId, count);
        return findMany(FIND_BY_FILM_QUERY, filmId, count);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Review> findAll(int count) {
        log.debug("Поиск всех отзывов, count={}", count);
        return findMany(FIND_ALL_QUERY, count);
    }

    @Override
    public void addLike(Long reviewId, Long userId, boolean isUseful) {
        Boolean current = jdbc.query(
                FIND_LIKE_QUERY,
                rs -> rs.next() ? rs.getBoolean("is_useful") : null,
                reviewId, userId);

        if (current != null && current == isUseful) {
            log.debug("Оценка уже стоит: review={}, user={}", reviewId, userId);
            return;
        }

        int delta;
        if (current == null) {
            jdbc.update(INSERT_LIKE_QUERY, reviewId, userId, isUseful);
            delta = isUseful ? 1 : -1;
        } else {
            jdbc.update(DELETE_LIKE_QUERY, reviewId, userId);
            jdbc.update(INSERT_LIKE_QUERY, reviewId, userId, isUseful);
            delta = isUseful ? 2 : -2;
        }

        jdbc.update(UPDATE_USEFUL_QUERY, delta, reviewId);
        log.debug("Оценка учтена: review={}, user={}, isUseful={}",
                reviewId, userId, isUseful);
    }

    @Override
    public void removeLike(Long reviewId, Long userId) {
        Boolean wasUseful = jdbc.query(
                FIND_LIKE_QUERY,
                rs -> rs.next() ? rs.getBoolean("is_useful") : null,
                reviewId, userId);

        if (wasUseful == null) {
            log.debug("Оценки нет: review={}, user={}", reviewId, userId);
            return;
        }

        jdbc.update(DELETE_LIKE_QUERY, reviewId, userId);
        int delta = wasUseful ? -1 : 1;
        jdbc.update(UPDATE_USEFUL_QUERY, delta, reviewId);
        log.debug("Оценка удалена: review={}, user={}, wasUseful={}",
                reviewId, userId, wasUseful);
    }

    @Override
    public boolean existsByUserAndFilm(Long userId, Long filmId) {
        Integer count = jdbc.queryForObject(EXISTS_REVIEW_QUERY, Integer.class, userId, filmId);
        return count != null && count > 0;
    }

    @Override
    public boolean existsLike(Long reviewId, Long userId) {
        Integer count = jdbc.queryForObject(EXISTS_LIKE_QUERY, Integer.class, reviewId, userId);
        return count != null && count > 0;
    }
}
