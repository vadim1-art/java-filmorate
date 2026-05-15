package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Service
public class FilmService {
    @Qualifier("filmDbStorage")
    private final FilmStorage filmStorage;
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public void addLike(Long filmId, Long userId) {
        filmStorage.findById(filmId).orElseThrow(() -> new NotFoundException("Film not found"));
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        jdbcTemplate.update("INSERT INTO likes (film_id, user_id) VALUES (?, ?)", filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        jdbcTemplate.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    public List<Film> topFilms(int limit) {
        String sql = "SELECT f.*, m.name AS mpa_name, COUNT(l.user_id) AS likes_count " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                "LEFT JOIN likes l ON f.film_id = l.film_id " +
                "GROUP BY f.film_id " +
                "ORDER BY likes_count DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("film_id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getLong("duration"));
            film.setMpa(new ru.yandex.practicum.filmorate.model.Mpa(rs.getInt("mpa_id"),
                    rs.getString("mpa_name")));
            return film;
        }, limit);

        films.forEach(this::loadGenres);
        return films;
    }

    public Film create(Film film) {
        filmStorage.save(film);
        saveGenres(film);
        return film;
    }

    public Film update(Film film) {
        filmStorage.findById(film.getId()).orElseThrow(() -> new NotFoundException("Film not found"));
        filmStorage.update(film);
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenres(film);
        return film;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) return;
        for (Genre genre : film.getGenres()) {
            jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                    film.getId(), genre.getId());
        }
    }

    private void loadGenres(Film film) {
        String sql = "SELECT g.* FROM genres g JOIN film_genres fg ON g.genre_id = fg.genre_id WHERE fg.film_id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) ->
                new Genre(rs.getInt("genre_id"), rs.getString("name")), film.getId());
        film.setGenres(new LinkedHashSet<>(genres));
    }

    public Collection<Film> getFilms() {
        Collection<Film> films = filmStorage.findAll();
        films.forEach(this::loadGenres);
        return films;
    }
}