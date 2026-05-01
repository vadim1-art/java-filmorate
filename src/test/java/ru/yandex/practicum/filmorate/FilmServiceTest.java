package ru.yandex.practicum.filmorate;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FilmServiceTest {

    private FilmService filmService;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @BeforeEach
    void setUp() {
        filmService = new FilmService();
    }

    @Test
    void createValidFilmShouldReturnFilmWithId() {
        Film film = new Film(null, "Interstellar", "Sci-fi epic",
                LocalDate.of(2014, 11, 7), 169L);
        Film created = filmService.create(film);
        assertNotNull(created.getId());
        assertEquals(1L, created.getId());
        assertTrue(filmService.getFilms().contains(created));
    }

    @Test
    void createFilmWithDuplicateNameShouldThrowException() {
        filmService.create(new Film(null, "Title",
                "desc", LocalDate.now(), 100L));
        DuplicatedDataException ex = assertThrows(DuplicatedDataException.class,
                () -> filmService.create(new Film(null, "Title",
                        "desc", LocalDate.now(), 100L)));
        assertEquals("Film name already in use", ex.getMessage());
    }

    @Test
    void createFilmWithDescriptionExactly200CharsShouldPass() {
        String desc = "a".repeat(200);
        Film film = new Film(null, "Title", desc, LocalDate.now(), 90L);
        assertDoesNotThrow(() -> filmService.create(film));
    }

    @Test
    void createFilmWithNullDescriptionShouldNotThrow() {
        Film film = new Film(null, "Title", null, LocalDate.now(), 120L);
        assertDoesNotThrow(() -> filmService.create(film));
    }

    @Test
    void updateExistingFilmShouldChangeFields() {
        Film original = filmService.create(
                new Film(null, "Original", "Desc",
                        LocalDate.of(2000, 1, 1), 100L));
        Film update = new Film(original.getId(), "Updated", "New desc",
                LocalDate.of(2001, 2, 2), 150L);
        Film result = filmService.update(update);
        assertEquals("Updated", result.getName());
        assertEquals("New desc", result.getDescription());
        assertEquals(LocalDate.of(2001, 2, 2), result.getReleaseDate());
        assertEquals(150L, result.getDuration());
    }

    @Test
    void updateNonExistentFilmShouldThrowNotFoundException() {
        Film film = new Film(999L, "Name", "Desc", LocalDate.now(), 120L);
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> filmService.update(film));
        assertEquals("Film with id 999 not found", ex.getMessage());
    }

    @Test
    void updateFilmWithDuplicateNameShouldThrowException() {
        Film first = filmService.create(new Film(null, "First",
                "Desc", LocalDate.now(), 100L));
        filmService.create(new Film(null, "Second",
                "Desc", LocalDate.now(), 100L));
        Film update = new Film(first.getId(), "Second",
                "irrelevant", LocalDate.now(), 100L);
        DuplicatedDataException ex = assertThrows(DuplicatedDataException.class,
                () -> filmService.update(update));
        assertEquals("Film name already in use", ex.getMessage());
    }

    @Test
    void updateFilmWithSameNameDifferentCaseShouldThrowException() {
        Film original = filmService.create(new Film(null, "Original",
                "Desc", LocalDate.now(), 100L));
        Film update = new Film(original.getId(), "ORIGINAL",
                "Desc", LocalDate.now(), 100L);
        DuplicatedDataException ex = assertThrows(DuplicatedDataException.class,
                () -> filmService.update(update));
        assertEquals("Film name already in use", ex.getMessage());
    }

    @Test
    void updateFilmWithNullNameShouldKeepOldName() {
        Film original = filmService.create(new Film(null, "Original",
                "Desc", LocalDate.now(), 100L));
        Film update = new Film(original.getId(), null,
                "new desc", null, 0L);
        Film result = filmService.update(update);
        assertEquals("Original", result.getName());
    }

    @Test
    void updateFilmWithBlankNameShouldKeepOldName() {
        Film original = filmService.create(new Film(null, "Original",
                "Desc", LocalDate.now(), 100L));
        Film update = new Film(original.getId(), "   ",
                "new desc", null, 0L);
        Film result = filmService.update(update);
        assertEquals("Original", result.getName());
    }

    @Test
    void updateFilmWithNullDescriptionShouldKeepOldDescription() {
        Film original = filmService.create(new Film(null, "Film",
                "Original desc", LocalDate.now(), 100L));
        Film update = new Film(original.getId(), "Film",
                null, null, 0L);
        Film result = filmService.update(update);
        assertEquals("Original desc", result.getDescription());
    }

    @Test
    void updateFilmWithBlankDescriptionShouldKeepOldDescription() {
        Film original = filmService.create(new Film(null, "Film",
                "Original desc", LocalDate.now(), 100L));
        Film update = new Film(original.getId(), "Film",
                "  ", null, 0L);
        Film result = filmService.update(update);
        assertEquals("Original desc", result.getDescription());
    }

    @Test
    void updateFilmWithNullReleaseDateShouldKeepOldReleaseDate() {
        LocalDate release = LocalDate.of(2000, 1, 1);
        Film original = filmService.create(new Film(null, "Film",
                "desc", release, 100L));
        Film update = new Film(original.getId(), "Film",
                "desc", null, 100L);
        Film result = filmService.update(update);
        assertEquals(release, result.getReleaseDate());
    }

    @Test
    void updateFilmWithNegativeDurationShouldKeepOldDuration() {
        Film original = filmService.create(new Film(null, "Film",
                "desc", LocalDate.now(), 120L));
        Film update = new Film(original.getId(), "Film",
                "desc", LocalDate.now(), -5L);
        Film result = filmService.update(update);
        assertEquals(120L, result.getDuration());
    }

    @Test
    void updateFilmWithZeroDurationShouldChangeDuration() {
        Film original = filmService.create(new Film(null, "Film",
                "desc", LocalDate.now(), 120L));
        Film update = new Film(original.getId(), "Film",
                "desc", LocalDate.now(), 0L);
        Film result = filmService.update(update);
        assertEquals(0L, result.getDuration());
    }

    @Test
    void filmNameMustNotBeBlank() {
        Film film = new Film(null, "   ", "desc", LocalDate.now(), 120L);
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Film.Create.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().contains("Film name cannot be empty")));
    }

    @Test
    void filmDescriptionMustNotExceed200Chars() {
        String desc = "a".repeat(201);
        Film film = new Film(null, "Title", desc, LocalDate.now(), 90L);
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Film.Create.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().contains("description is too long")));
    }

    @Test
    void releaseDateMustNotBeBefore28Dec1895() {
        Film film = new Film(null, "Old", "desc",
                LocalDate.of(1895, 12, 27), 10L);
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Film.Create.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().contains("Release date must not be before")));
    }

    @Test
    void releaseDateMustNotBeNull() {
        Film film = new Film(null, "Title", "desc", null, 120L);
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Film.Create.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().contains("Release date is required")));
    }

    @Test
    void durationMustBePositive() {
        Film film = new Film(null, "Film", "desc", LocalDate.now(), 0L);
        Set<ConstraintViolation<Film>> violations = validator.validate(film, Film.Create.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().contains("Duration must be positive")));
    }
}