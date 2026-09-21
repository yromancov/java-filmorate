package ru.yandex.practicum.filmorate.storage.review;

import ru.yandex.practicum.filmorate.model.Review;

import java.util.List;
import java.util.Optional;

public interface ReviewStorage {

    Review add(Review review);

    Review update(Review review);

    boolean delete(Long id);

    Optional<Review> findById(Long id);

    List<Review> findByFilmId(Long filmId, int count);

    List<Review> findAll(int count);

    void addLike(Long reviewId, Long userId, boolean isUseful);

    void removeLike(Long reviewId, Long userId);

    boolean existsByUserAndFilm(Long userId, Long filmId);

}