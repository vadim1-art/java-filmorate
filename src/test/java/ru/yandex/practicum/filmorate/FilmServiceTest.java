package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.InvalidDateException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {

    private FilmService filmService;

    @BeforeEach
    void setUp() {
        filmService = new FilmService();
    }

    @Test
    void createValidFilmShouldReturnFilmWithId() {
        Film film = new Film(null, "Interstellar", "Sci-fi epic",
                LocalDate.of(2014, 11, 7), 169);
        Film created = filmService.create(film);
        assertNotNull(created.getId());
        assertEquals(1L, created.getId());
        assertTrue(filmService.getFilms().contains(created));
    }

    @Test
    void createFilmWithNullNameShouldThrowValidationException() {
        Film film = new Film(null, null, "desc", LocalDate.now(), 120);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> filmService.create(film));
        assertEquals("Film name cannot be empty", ex.getMessage());
    }

    @Test
    void createFilmWithBlankNameShouldThrowValidationException() {
        Film film = new Film(null, "   ", "desc", LocalDate.now(), 120);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> filmService.create(film));
        assertEquals("Film name cannot be empty", ex.getMessage());
    }

    @Test
    void createFilmWithDescriptionExactly200CharsShouldPass() {
        String desc = "a".repeat(200);
        Film film = new Film(null, "Title", desc, LocalDate.now(), 90);
        assertDoesNotThrow(() -> filmService.create(film));
    }

    @Test
    void createFilmWithDescriptionOver200CharsShouldThrowValidationException() {
        String desc = "a".repeat(201);
        Film film = new Film(null, "Title", desc, LocalDate.now(), 90);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> filmService.create(film));
        assertEquals("Film description is too long", ex.getMessage());
    }

    @Test
    void createFilmWithNullDescriptionShouldNotThrow() {
        Film film = new Film(null, "Title", null, LocalDate.now(), 120);
        assertDoesNotThrow(() -> filmService.create(film));
    }

    @Test
    void createFilmWithReleaseDateBefore28Dec1895ShouldThrowInvalidDateException() {
        Film film = new Film(null, "Old", "desc",
                LocalDate.of(1895, 12, 27), 10);
        InvalidDateException ex = assertThrows(InvalidDateException.class,
                () -> filmService.create(film));
        assertEquals("Film release date is too old", ex.getMessage());
    }

    @Test
    void createFilmWithReleaseDateExactly28Dec1895ShouldPass() {
        Film film = new Film(null, "Old", "desc",
                LocalDate.of(1895, 12, 28), 10);
        assertDoesNotThrow(() -> filmService.create(film));
    }

    @Test
    void createFilmWithNullReleaseDateShouldThrowValidationException() {
        Film film = new Film(null, "Title", "desc", null, 120);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> filmService.create(film));
        assertEquals("Release date is required", ex.getMessage());
    }

    @Test
    void createFilmWithZeroDurationShouldThrowInvalidDateException() {
        Film film = new Film(null, "Film", "desc", LocalDate.now(), 0);
        InvalidDateException ex = assertThrows(InvalidDateException.class,
                () -> filmService.create(film));
        assertEquals("Film duration is too low", ex.getMessage());
    }

    @Test
    void createFilmWithNegativeDurationShouldThrowInvalidDateException() {
        Film film = new Film(null, "Film", "desc", LocalDate.now(), -10);
        InvalidDateException ex = assertThrows(InvalidDateException.class,
                () -> filmService.create(film));
        assertEquals("Film duration is too low", ex.getMessage());
    }

    @Test
    void createFilmWithPositiveDurationShouldPass() {
        Film film = new Film(null, "Film", "desc", LocalDate.now(), 1);
        assertDoesNotThrow(() -> filmService.create(film));
    }

    @Test
    void updateExistingFilmShouldChangeFields() {
        Film original = filmService.create(
                new Film(null, "Original", "Desc",
                        LocalDate.of(2000, 1, 1), 100));
        Film update = new Film(original.getId(), "Updated", "New desc",
                LocalDate.of(2001, 2, 2), 150);
        Film result = filmService.update(update);
        assertEquals("Updated", result.getName());
        assertEquals("New desc", result.getDescription());
        assertEquals(LocalDate.of(2001, 2, 2), result.getReleaseDate());
        assertEquals(150, result.getDuration());
    }

    @Test
    void updateFilmWithNullIdShouldThrowValidationException() {
        Film film = new Film(null, "Name", "Desc", LocalDate.now(), 120);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> filmService.update(film));
        assertEquals("Film id is null", ex.getMessage());
    }

    @Test
    void updateNonExistentFilmShouldThrowException() {
        Film film = new Film(999L, "Name", "Desc", LocalDate.now(), 120);
        DuplicatedDataException ex = assertThrows(DuplicatedDataException.class,
                () -> filmService.update(film));
        assertEquals("Film with id 999 not found", ex.getMessage());
    }

    @Test
    void updateFilmWithDuplicateNameShouldThrowException() {
        Film first = filmService.create(
                new Film(null, "First", "Desc", LocalDate.now(), 100));
        Film second = filmService.create(
                new Film(null, "Second", "Desc", LocalDate.now(), 100));

        Film update = new Film(second.getId(), "First", "irrelevant",
                LocalDate.now(), 100);
        DuplicatedDataException ex = assertThrows(DuplicatedDataException.class,
                () -> filmService.update(update));
        assertEquals("Film name already in use", ex.getMessage());
    }

    @Test
    void updateFilmWithSameNameDifferentCaseShouldThrowException() {
        Film original = filmService.create(
                new Film(null, "Original", "Desc", LocalDate.now(), 100));
        Film update = new Film(original.getId(), "ORIGINAL", "Desc",
                LocalDate.now(), 100);
        DuplicatedDataException ex = assertThrows(DuplicatedDataException.class,
                () -> filmService.update(update));
        assertEquals("Film name already in use", ex.getMessage());
    }

    @Test
    void updateFilmWithNullNameShouldKeepOldName() {
        Film original = filmService.create(
                new Film(null, "Original", "Desc", LocalDate.now(), 100));
        Film update = new Film(original.getId(), null, "new desc", null, 0);
        Film result = filmService.update(update);
        assertEquals("Original", result.getName());
    }

    @Test
    void updateFilmWithBlankNameShouldKeepOldName() {
        Film original = filmService.create(
                new Film(null, "Original", "Desc", LocalDate.now(), 100));
        Film update = new Film(original.getId(), "   ", "new desc", null, 0);
        Film result = filmService.update(update);
        assertEquals("Original", result.getName());
    }

    @Test
    void updateFilmWithNullDescriptionShouldKeepOldDescription() {
        Film original = filmService.create(
                new Film(null, "Film", "Original desc", LocalDate.now(), 100));
        Film update = new Film(original.getId(), "Film", null, null, 0);
        Film result = filmService.update(update);
        assertEquals("Original desc", result.getDescription());
    }

    @Test
    void updateFilmWithBlankDescriptionShouldKeepOldDescription() {
        Film original = filmService.create(
                new Film(null, "Film", "Original desc", LocalDate.now(), 100));
        Film update = new Film(original.getId(), "Film", "  ", null, 0);
        Film result = filmService.update(update);
        assertEquals("Original desc", result.getDescription());
    }

    @Test
    void updateFilmWithNullReleaseDateShouldKeepOldReleaseDate() {
        LocalDate release = LocalDate.of(2000, 1, 1);
        Film original = filmService.create(
                new Film(null, "Film", "desc", release, 100));
        Film update = new Film(original.getId(), "Film", "desc", null, 100);
        Film result = filmService.update(update);
        assertEquals(release, result.getReleaseDate());
    }

    @Test
    void updateFilmWithNegativeDurationShouldKeepOldDuration() {
        Film original = filmService.create(
                new Film(null, "Film", "desc", LocalDate.now(), 120));
        Film update = new Film(original.getId(), "Film", "desc",
                LocalDate.now(), -5);
        Film result = filmService.update(update);
        assertEquals(120, result.getDuration());
    }

    @Test
    void updateFilmWithZeroDurationShouldChangeDuration() {
        Film original = filmService.create(
                new Film(null, "Film", "desc", LocalDate.now(), 120));
        Film update = new Film(original.getId(), "Film", "desc",
                LocalDate.now(), 0);
        Film result = filmService.update(update);
        assertEquals(0, result.getDuration());
    }
}