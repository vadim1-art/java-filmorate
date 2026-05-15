package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class FilmServiceTest {

    private final FilmService filmService;

    @Test
    void createValidFilmShouldReturnFilmWithId() {
        Film film = new Film(null, "Interstellar", "Sci-fi epic",
                LocalDate.of(2014, 11, 7),
                169L, new Mpa(1, "G"), null);
        Film created = filmService.create(film);

        assertNotNull(created.getId());
        assertEquals("Interstellar", created.getName());
    }

    @Test
    void createTwoFilmsWithSameNameShouldBeAllowed() {
        filmService.create(new Film(null, "Avatar", "desc",
                LocalDate.now(), 100L, new Mpa(1, "G"), null));

        assertDoesNotThrow(() ->
                filmService.create(new Film(null, "Avatar", "another desc",
                        LocalDate.now(), 100L, new Mpa(1, "G"), null))
        );
    }

    @Test
    void updateExistingFilmShouldChangeFields() {
        Film original = filmService.create(new Film(null, "Original", "Desc",
                LocalDate.of(2000, 1, 1),
                100L, new Mpa(1, "G"), null));

        Film update = new Film(original.getId(), "Updated", "New desc",
                LocalDate.of(2001, 2, 2),
                150L, new Mpa(2, "PG"), null);

        Film result = filmService.update(update);

        assertEquals("Updated", result.getName());
        assertEquals(2, result.getMpa().getId());
    }

    @Test
    void updateNonExistentFilmShouldThrowNotFoundException() {
        Film film = new Film(999L, "Name", "Desc",
                LocalDate.now(), 120L, new Mpa(1, "G"), null);
        assertThrows(NotFoundException.class, () -> filmService.update(film));
    }
}