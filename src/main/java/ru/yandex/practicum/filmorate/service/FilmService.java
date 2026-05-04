package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;   // нужно для проверки существования пользователя

    public void addLike(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Film with id " + filmId + " not found"));
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
        film.getLikesUnderFilm().add(userId);   // теперь userId!
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Film with id " + filmId + " not found"));
        userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
        film.getLikesUnderFilm().remove(userId);
    }

    public Collection<Film> topFilms(int limit) {
        return filmStorage.findAll().stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikesUnderFilm().size()).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Collection<Film> getFilms() {
        return filmStorage.findAll();
    }

    public Film update(Film newFilm) {
        Film oldFilm = filmStorage.findById(newFilm.getId())
                .orElseThrow(() -> new NotFoundException("Film with id " + newFilm.getId() + " not found"));

        if (newFilm.getName() != null && !newFilm.getName().isBlank()
                && !newFilm.getName().equals(oldFilm.getName())) {
            boolean nameExists = filmStorage.findAll().stream()
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
        return filmStorage.update(oldFilm);
    }

    public Film create(Film film) {
        boolean nameExists = filmStorage.findAll().stream()
                .anyMatch(f -> f.getName().equalsIgnoreCase(film.getName()));
        if (nameExists) {
            throw new DuplicatedDataException("Film name already in use");
        }

        film.setId(getNextId());
        filmStorage.save(film);
        log.info("Created film: {}", film);
        return film;
    }

    private long getNextId() {
        return filmStorage.findAll().stream()
                .mapToLong(Film::getId)
                .max()
                .orElse(0) + 1;
    }
}