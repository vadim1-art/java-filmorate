package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
public class GenreController {
    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public Collection<Genre> findAll() {
        return jdbcTemplate.query("SELECT * FROM genres ORDER BY genre_id",
                (rs, rowNum) -> new Genre(rs.getInt("genre_id"),
                        rs.getString("name")));
    }

    @GetMapping("/{id}")
    public Genre findById(@PathVariable Integer id) {
        return jdbcTemplate.query("SELECT * FROM genres WHERE genre_id = ?",
                        (rs, rowNum) -> new Genre(rs.getInt("genre_id"),
                                rs.getString("name")), id)
                .stream().findFirst().orElseThrow(() -> new NotFoundException("Genre not found"));
    }
}