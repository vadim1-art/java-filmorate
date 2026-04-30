package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.InvalidDateException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
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
    private static final int MAX_DESCRIPTION_LENGTH = 200;

    public Collection<Film> getFilms() {
        return films.values();
    }

    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException("Film id is null");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            if (newFilm.getName() != null && !newFilm.getName().isBlank()) {
                if (!newFilm.getName().equals(oldFilm.getName())) {
                    boolean nameExists = films.values().stream()
                            .anyMatch(f -> f.getName().equalsIgnoreCase(newFilm.getName()));
                    if (nameExists) {
                        throw new DuplicatedDataException("Film name already in use");
                    }
                    oldFilm.setName(newFilm.getName());
                }
            }
            if (newFilm.getDescription() != null && !newFilm.getDescription().isBlank()) {
                oldFilm.setDescription(newFilm.getDescription());
            }
            if (newFilm.getReleaseDate() != null) {
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }
            if (newFilm.getDuration() >= 0) {
                oldFilm.setDuration(newFilm.getDuration());
            }
            return oldFilm;
        }
        throw new DuplicatedDataException("Film with id " + newFilm.getId() + " not found");
    }

    public Film create(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            log.warn("Попытка создать фильм с пустым названием");
            throw new ValidationException("Film name cannot be empty");
        }
        if (film.getDescription() != null && film.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
            log.warn("Слишком длинное описание фильма (длина: {} символов)", film.getDescription().length());
            throw new ValidationException("Film description is too long");
        }
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Release date is required");
        }
        if (film.getReleaseDate().isBefore(MIN_DATE)) {
            log.warn("Дата релиза фильма {} ранее {}", film.getReleaseDate(), MIN_DATE);
            throw new InvalidDateException("Film release date is too old");
        }
        if (film.getDuration() <= 0) {
            log.warn("Продолжительность фильма <= 0: {}", film.getDuration());
            throw new InvalidDateException("Film duration is too low");
        }

        film.setId(getNextId());
        films.put(film.getId(), film);
        return film;
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}