package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.InvalidDateException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class FilmService {

    private final Map<Long, Film> films = new HashMap<>();
    private static final LocalDate MIN_DATE = LocalDate.of(1895, 12, 28);


    public Collection<Film> getFilms() {
        return films.values();
    }

    public Film update(Film newFilm) {
        Film oldFilm = films.get(newFilm.getId());
        if (oldFilm == null) {
            throw new NotFoundException("Film with id " + newFilm.getId() + " not found");
        }

        if (newFilm.getName() != null && !newFilm.getName().isBlank()
                && !newFilm.getName().equals(oldFilm.getName())) {
            boolean nameExists = films.values().stream()
                    .anyMatch(f -> f.getName().equalsIgnoreCase(newFilm.getName()));
            if (nameExists) {
                throw new DuplicatedDataException("Film name already in use");
            }
            oldFilm.setName(newFilm.getName());
        }

        if (newFilm.getDescription() != null && !newFilm.getDescription().isBlank()) {
            oldFilm.setDescription(newFilm.getDescription());
        }
        if (newFilm.getReleaseDate() != null) {
            oldFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() != null && newFilm.getDuration() >= 0) {
            oldFilm.setDuration(newFilm.getDuration());
        }
        return oldFilm;
    }

    public Film create(Film film) {
        boolean nameExists = films.values().stream()
                .anyMatch(f -> f.getName().equalsIgnoreCase(film.getName()));
        if (nameExists) {
            throw new DuplicatedDataException("Film name already in use");
        }
        if (film.getReleaseDate().isBefore(MIN_DATE)) {
            throw new InvalidDateException("Film release date is too old");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Created film: {}", film);
        return film;
    }

    private long getNextId() {
        return films.keySet().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0) + 1;
    }
}