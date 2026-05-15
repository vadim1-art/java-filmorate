package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    public Film create(Film film) {
        Film savedFilm = filmStorage.save(film);
        saveGenres(film);
        log.info("Добавлен новый фильм: {}", savedFilm);
        return getFilmById(savedFilm.getId());
    }

    public Film update(Film film) {
        filmStorage.findById(film.getId())
                .orElseThrow(() -> new NotFoundException("Фильм с id " + film.getId() + " не найден"));

        filmStorage.update(film);

        // Обновляем жанры: сначала удаляем старые, потом пишем новые
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenres(film);

        log.info("Обновлен фильм: {}", film);
        return getFilmById(film.getId());
    }

    public Film getFilmById(Long id) {
        Film film = filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
        loadGenres(film);
        return film;
    }

    public Collection<Film> getFilms() {
        Collection<Film> films = filmStorage.findAll();
        films.forEach(this::loadGenres);
        return films;
    }

    public void addLike(Long filmId, Long userId) {
        getFilmById(filmId); // Проверка на существование
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(Long filmId, Long userId) {
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
        log.info("Пользователь {} удалил лайк у фильма {}", userId, filmId);
    }

    public List<Film> topFilms(int limit) {
        String sql = "SELECT f.*, m.name AS mpa_name " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN likes l ON f.film_id = l.film_id " +
                "GROUP BY f.film_id " +
                "ORDER BY COUNT(l.user_id) DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("film_id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getLong("duration"));
            film.setMpa(new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")));
            return film;
        }, limit);

        films.forEach(this::loadGenres);
        return films;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            return;
        }
        List<Integer> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .distinct()
                .collect(Collectors.toList());

        for (Integer genreId : genreIds) {
            jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                    film.getId(), genreId);
        }
    }

    private void loadGenres(Film film) {
        String sql = "SELECT g.genre_id, g.name FROM genres g " +
                "JOIN film_genres fg ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.genre_id";
        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("genre_id"), rs.getString("name")), film.getId());
        film.setGenres(new LinkedHashSet<>(genres));
    }
}