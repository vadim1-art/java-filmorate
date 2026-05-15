package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Optional;

@Component("filmDbStorage")
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void save(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setLong(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?," +
                " release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), film.getId());
        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT f.*, m.name AS mpa_name FROM films f JOIN mpa_ratings m " +
                "ON f.mpa_id = m.mpa_id WHERE f.film_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToFilm, id).stream().findFirst();
    }

    @Override
    public Collection<Film> findAll() {
        String sql = "SELECT f.*, m.name AS mpa_name FROM films f JOIN mpa_ratings m ON f.mpa_id = m.mpa_id";
        return jdbcTemplate.query(sql, this::mapRowToFilm);
    }

    @Override
    public Collection<Long> getKey() {
        return jdbcTemplate.query("SELECT film_id FROM films", (rs, rowNum) ->
                rs.getLong("film_id"));
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getLong("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getLong("duration"));
        film.setMpa(new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")));
        return film;
    }
}