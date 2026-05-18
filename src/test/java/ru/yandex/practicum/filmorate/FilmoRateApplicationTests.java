package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, FilmDbStorage.class})
class FilmoRateApplicationTests {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;


    @Test
    public void testSaveAndFindUserById() {
        User newUser = new User(null, "user@test.com", "vlad", "Vladimir",
                LocalDate.of(1990, 5, 10));
        userStorage.save(newUser);

        Optional<User> userOptional = userStorage.findById(newUser.getId());

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", newUser.getId())
                                .hasFieldOrPropertyWithValue("email", "user@test.com")
                );
    }

    @Test
    public void testUpdateUser() {
        User user = new User(null, "old@test.com", "old", "Old Name",
                LocalDate.of(1980, 1, 1));
        userStorage.save(user);

        user.setName("New Name");
        user.setEmail("new@test.com");
        userStorage.update(user);

        Optional<User> updatedUser = userStorage.findById(user.getId());

        assertThat(updatedUser)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u).hasFieldOrPropertyWithValue("name", "New Name")
                                .hasFieldOrPropertyWithValue("email", "new@test.com")
                );
    }

    @Test
    public void testFindAllUsers() {
        userStorage.save(new User(null, "1@test.com", "l1", "n1",
                LocalDate.now().minusYears(20)));
        userStorage.save(new User(null, "2@test.com", "l2", "n2",
                LocalDate.now().minusYears(20)));

        Collection<User> users = userStorage.findAll();
        assertThat(users).hasSize(2);
    }


    @Test
    public void testSaveAndFindFilmById() {
        Film newFilm = new Film(null, "Inception", "Dream within a dream",
                LocalDate.of(2010, 7, 16), 148L, new Mpa(1, "G"), null);
        filmStorage.save(newFilm);

        Optional<Film> filmOptional = filmStorage.findById(newFilm.getId());

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("name", "Inception")
                                .hasFieldOrPropertyWithValue("duration", 148L)
                );
    }

    @Test
    public void testUpdateFilm() {
        Film film = new Film(null, "Movie", "Desc",
                LocalDate.now(), 100L, new Mpa(1, "G"), null);
        filmStorage.save(film);

        film.setName("Updated Movie");
        film.setMpa(new Mpa(3, "PG-13"));
        filmStorage.update(film);

        Optional<Film> updatedFilm = filmStorage.findById(film.getId());

        assertThat(updatedFilm)
                .isPresent()
                .hasValueSatisfying(f ->
                        assertThat(f).hasFieldOrPropertyWithValue("name", "Updated Movie")
                                .hasFieldOrPropertyWithValue("mpa.id", 3)
                );
    }

    @Test
    public void testFindAllFilms() {
        filmStorage.save(new Film(null, "F1", "D1",
                LocalDate.now(), 100L, new Mpa(1, "G"), null));
        filmStorage.save(new Film(null, "F2", "D2",
                LocalDate.now(), 120L, new Mpa(2, "PG"), null));

        Collection<Film> films = filmStorage.findAll();
        assertThat(films).hasSize(2);
    }
}