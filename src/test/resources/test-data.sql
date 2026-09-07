INSERT INTO films (title, description, releaseDate, duration, age_rating_id)
VALUES ('Матрица', 'Хакер узнаёт правду о реальности', '1999-03-31', 136, 4),
       ('Король Лев', 'История львёнка Симбы', '1994-06-24', 88, 1);

INSERT INTO users (name, login, email, birthday)
VALUES ('Иван', 'ivan', 'ivan@mail.ru', '1990-05-15'),
       ('Мария', 'maria', 'maria@mail.ru', '1995-11-02');

INSERT INTO film_genre (film_id, genre_id)
VALUES (1, 6),
       (2, 3);

INSERT INTO likesfilms (film_id, user_id)
VALUES (1, 1),
       (1, 2),
       (2, 1);

INSERT INTO follows (following_user_id, followed_user_id, status)
VALUES (1, 2, 'CONFIRM');