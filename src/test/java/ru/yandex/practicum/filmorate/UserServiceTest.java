package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserServiceTest {

    private final UserService userService;

    @Test
    void createValidUserShouldReturnUserWithId() {
        User user = new User(null, "test@ya.ru", "login", "name",
                LocalDate.now().minusYears(20));
        User created = userService.create(user);

        assertNotNull(created.getId());
    }

    @Test
    void createUserWithEmptyNameShouldUseLoginAsName() {
        User user = new User(null, "test@mail.ru", "MyLogin", "",
                LocalDate.now().minusYears(20));
        User created = userService.create(user);

        assertEquals("MyLogin", created.getName());
    }

    @Test
    void updateNonExistentUserShouldThrowNotFoundException() {
        User user = new User(999L, "a@b.com", "login", "name",
                LocalDate.now().minusYears(10));
        assertThrows(NotFoundException.class, () -> userService.update(user));
    }
}