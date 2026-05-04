package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;


    public void addLike(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Film not found"));
        film.getLikesUnderFilm().add(userId);
    }

    public void removeLike(Long filmId, Long userId) {
        Film film = filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException("Film not found"));
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

    public Film update(Film updatedFilm ) {
        Film oldFilm = filmStorage.findById(updatedFilm.getId())
                .orElseThrow(() -> new NotFoundException("Film not found"));
        if (oldFilm == null) {
            throw new NotFoundException("Film with id " + updatedFilm.getId() + " not found");
        }

        if (updatedFilm.getName() != null && !updatedFilm.getName().isBlank()
                && !updatedFilm.getName().equals(oldFilm.getName())) {
            boolean nameExists = filmStorage.findAll().stream()
                    .anyMatch(f -> f.getName().equalsIgnoreCase(updatedFilm.getName()));
            if (nameExists) {
                throw new DuplicatedDataException("Film name already in use");
            }
            oldFilm.setName(updatedFilm.getName());
        }

        if (updatedFilm.getDescription() != null && !updatedFilm.getDescription().isBlank()) {
            oldFilm.setDescription(updatedFilm.getDescription());
        }
        if (updatedFilm.getReleaseDate() != null) {
            oldFilm.setReleaseDate(updatedFilm.getReleaseDate());
        }
        if (updatedFilm.getDuration() != null && updatedFilm.getDuration() >= 0) { // Без newFilm.getDuration() >= 0 тест падает в GitHub
            oldFilm.setDuration(updatedFilm.getDuration());
        }
        return filmStorage.update(oldFilm);
    }

    public Film create(Film film) {
        boolean nameExists = filmStorage.findAll().stream()    // Проверка уникальности названия
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
        return filmStorage.getKey().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0) + 1;
    }
}