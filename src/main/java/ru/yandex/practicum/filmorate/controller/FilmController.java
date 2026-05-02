package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.validation.Create;
import ru.yandex.practicum.filmorate.validation.Update;

import java.util.Collection;


@RequiredArgsConstructor
@RestController
@RequestMapping("/films")
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> getFilms() {
        return filmService.getFilms();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public Film create(@Validated(Create.class) @RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Validated(Update.class) @RequestBody Film film) {
        return filmService.update(film);
    }
}