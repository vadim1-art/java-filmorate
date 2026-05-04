package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {
    void save(Film film);
    Film update(Film film);
    Optional<Film> findById(Long id);
    Collection<Film> findAll();
    Collection<Long> getKey();
}
