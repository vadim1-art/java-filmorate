package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;


@Component
public class InMemoryFilmStorage implements  FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public void save(Film film) {
        films.put(film.getId(), film);
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Collection<Long> getKey() {
        return films.keySet();
    }
}
