package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.Collection;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaController {
    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    public Collection<Mpa> findAll() {
        return jdbcTemplate.query("SELECT * FROM mpa_ratings ORDER BY mpa_id",
                (rs, rowNum) -> new Mpa(rs.getInt("mpa_id"),
                        rs.getString("name")));
    }

    @GetMapping("/{id}")
    public Mpa findById(@PathVariable Integer id) {
        return jdbcTemplate.query("SELECT * FROM mpa_ratings WHERE mpa_id = ?",
                        (rs, rowNum) -> new Mpa(rs.getInt("mpa_id"),
                                rs.getString("name")), id)
                .stream().findFirst().orElseThrow(() -> new NotFoundException("MPA not found"));
    }
}