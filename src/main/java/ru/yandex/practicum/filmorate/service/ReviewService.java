package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.EventType;
import ru.yandex.practicum.filmorate.model.Operation;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewStorage reviewStorage;
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final FeedService feedService;

    public Review add(Review review) {
        log.info("Добавление отзыва: user={}, film={}",
                review.getUserId(), review.getFilmId());

        validateUserExists(review.getUserId());
        validateFilmExists(review.getFilmId());

        if (reviewStorage.existsByUserAndFilm(review.getUserId(), review.getFilmId())) {
            throw new ValidationException("Пользователь уже оставил отзыв на этот фильм");
        }

        Review saved = reviewStorage.add(review);
        feedService.addEvent(saved.getUserId(), EventType.REVIEW, Operation.ADD, saved.getReviewId());
        log.info("Отзыв создан с id={}", saved.getReviewId());
        return saved;
    }

    public Review update(Review review) {
        log.info("Обновление отзыва id={}", review.getReviewId());

        if (review.getReviewId() == null) {
            throw new ValidationException("Id отзыва должен быть указан");
        }

        validateReviewExists(review.getReviewId());

        Review updated = reviewStorage.update(review);
        feedService.addEvent(updated.getUserId(), EventType.REVIEW, Operation.UPDATE, updated.getReviewId());
        log.info("Отзыв id={} обновлён", updated.getReviewId());
        return updated;
    }

    public void delete(Long id) {
        log.info("Удаление отзыва id={}", id);

        Review review = reviewStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id=" + id + " не найден"));

        reviewStorage.delete(id);
        feedService.addEvent(review.getUserId(), EventType.REVIEW, Operation.REMOVE, id);
        log.info("Отзыв id={} удалён", id);
    }

    public Review findById(Long id) {
        log.debug("Поиск отзыва по id={}", id);
        return reviewStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(
                        "Отзыв с id=" + id + " не найден"));
    }

    public List<Review> findByFilmId(Long filmId, int count) {
        log.debug("Поиск отзывов: filmId={}, count={}", filmId, count);

        return filmId == null
                ? reviewStorage.findAll(count)
                : reviewStorage.findByFilmId(filmId, count);
    }

    public void addLike(Long reviewId, Long userId, boolean isUseful) {
        validateReviewExists(reviewId);
        validateUserExists(userId);

        reviewStorage.addLike(reviewId, userId, isUseful);
        log.info("Оценка добавлена: review={}, user={}, isUseful={}",
                reviewId, userId, isUseful);
    }

    public void removeLike(Long reviewId, Long userId) {
        validateReviewExists(reviewId);
        validateUserExists(userId);

        reviewStorage.removeLike(reviewId, userId);
        log.info("Оценка удалена: review={}, user={}", reviewId, userId);
    }

    private void validateUserExists(Long userId) {
        if (userStorage.getUser(userId).isEmpty()) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден");
        }
    }

    private void validateFilmExists(Long filmId) {
        if (filmStorage.getFilm(filmId).isEmpty()) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден");
        }
    }

    private void validateReviewExists(Long reviewId) {
        if (reviewStorage.findById(reviewId).isEmpty()) {
            throw new NotFoundException("Отзыв с id=" + reviewId + " не найден");
        }
    }
}
