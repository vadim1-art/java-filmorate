package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FilmService {

    private final FilmStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;

    public Film create(Film film) {
        if (film.getMpa() != null) {
            checkMpaExists(film.getMpa().getId());
        }
        if (film.getGenres() != null) {
            checkGenresExist(new ArrayList<>(film.getGenres()));
        }

        Film savedFilm = filmStorage.save(film);
        saveGenres(savedFilm.getId(), film.getGenres());

        log.info("Добавлен новый фильм: {}", savedFilm);
        return getFilmById(savedFilm.getId());
    }

    public Film update(Film film) {
        getFilmById(film.getId());

        if (film.getMpa() != null) {
            checkMpaExists(film.getMpa().getId());
        }
        if (film.getGenres() != null) {
            checkGenresExist(new ArrayList<>(film.getGenres()));
        }

        Film updatedFilm = filmStorage.update(film);

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        saveGenres(updatedFilm.getId(), film.getGenres());

        log.info("Фильм с id {} успешно обновлен", film.getId());
        return getFilmById(updatedFilm.getId());
    }

    public Collection<Film> getFilms() {
        Collection<Film> films = filmStorage.findAll();
        loadGenresForFilms(films);
        return films;
    }

    public Film getFilmById(Long id) {
        Film film = filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм с id " + id + " не найден"));
        loadGenresForFilms(List.of(film));
        return film;
    }

    private void checkMpaExists(int mpaId) {
        String sql = "SELECT COUNT(*) FROM mpa_ratings WHERE mpa_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, mpaId);
        if (count == null || count == 0) {
            throw new NotFoundException("MPA рейтинг с id " + mpaId + " не найден");
        }
    }

    private void checkGenresExist(List<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        List<Integer> genreIds = genres.stream()
                .map(Genre::getId)
                .distinct()
                .collect(Collectors.toList());

        String inSql = String.join(",", Collections.nCopies(genreIds.size(), "?"));
        String sql = "SELECT COUNT(*) FROM genres WHERE genre_id IN (" + inSql + ")";

        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, genreIds.toArray());
        if (count == null || count < genreIds.size()) {
            throw new NotFoundException("Один или несколько жанров не найдены в базе");
        }
    }

    private void saveGenres(Long filmId, Collection<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        List<Integer> uniqueGenreIds = genres.stream()
                .map(Genre::getId)
                .distinct()
                .collect(Collectors.toList());

        String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
        List<Object[]> batchArgs = uniqueGenreIds.stream()
                .map(genreId -> new Object[]{filmId, genreId})
                .collect(Collectors.toList());

        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    private void loadGenresForFilms(Collection<Film> films) {
        if (films == null || films.isEmpty()) {
            return;
        }

        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = "SELECT fg.film_id, g.genre_id, g.name " +
                "FROM genres g " +
                "JOIN film_genres fg ON g.genre_id = fg.genre_id " +
                "WHERE fg.film_id IN (" + inSql + ")";

        Map<Long, List<Genre>> genresByFilmId = jdbcTemplate.query(sql, rs -> {
            Map<Long, List<Genre>> result = new HashMap<>();
            while (rs.next()) {
                Long filmId = rs.getLong("film_id");
                Genre genre = new Genre(rs.getInt("genre_id"), rs.getString("name"));
                result.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
            }
            return result;
        }, filmIds.toArray());

        films.forEach(film -> {
            List<Genre> genres = genresByFilmId.getOrDefault(film.getId(), new ArrayList<>());
            film.setGenres(new LinkedHashSet<>(genres));
        });
    }
}