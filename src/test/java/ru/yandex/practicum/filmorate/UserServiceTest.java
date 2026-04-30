package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.InvalidDateException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
    }

    @Test
    void createValidUserShouldReturnUserWithId() {
        User user = new User(null, "test@ya.ru", "login", "name",
                LocalDate.now().minusDays(365));
        User created = userService.create(user);
        assertNotNull(created.getId());
        assertEquals("test@ya.ru", created.getEmail());
    }

    @Test
    void createUserWithNullEmailShouldThrowValidationException() {
        User user = new User(null, null, "login", "name", LocalDate.now());
        ValidationException ex = assertThrows(ValidationException.class,
                () -> userService.create(user));
        assertEquals("Email is required", ex.getMessage());
    }

    @Test
    void createUserWithBlankEmailShouldThrowValidationException() {
        User user = new User(null, "   ", "login", "name", LocalDate.now());
        ValidationException ex = assertThrows(ValidationException.class,
                () -> userService.create(user));
        assertEquals("Email is required", ex.getMessage());
    }

    @Test
    void createUserWithEmailWithoutAtShouldThrowValidationException() {
        User user = new User(null, "email", "login", "name", LocalDate.now());
        ValidationException ex = assertThrows(ValidationException.class,
                () -> userService.create(user));
        assertEquals("Email must contain '@'", ex.getMessage());
    }

    @Test
    void createUserWithNullLoginShouldThrowValidationException() {
        User user = new User(null, "test@mail.ru", null, "name", LocalDate.now());
        ValidationException ex = assertThrows(ValidationException.class,
                () -> userService.create(user));
        assertEquals("Login is required", ex.getMessage());
    }

    @Test
    void createUserWithBlankLoginShouldThrowValidationException() {
        User user = new User(null, "test@mail.ru", "   ", "name", LocalDate.now());
        ValidationException ex = assertThrows(ValidationException.class,
                () -> userService.create(user));
        assertEquals("Login is required", ex.getMessage());
    }

    @Test
    void createUserWithEmptyNameShouldUseLoginAsName() {
        User user = new User(null, "test@mail.ru", "MyLogin", "",
                LocalDate.now().minusDays(365));
        User created = userService.create(user);
        assertEquals("MyLogin", created.getName());
    }

    @Test
    void createUserWithNullNameShouldUseLoginAsName() {
        User user = new User(null, "test@mail.ru", "MyLogin", null,
                LocalDate.now().minusDays(365));
        User created = userService.create(user);
        assertEquals("MyLogin", created.getName());
    }

    @Test
    void createUserWithBirthdayInFutureShouldThrowInvalidDateException() {
        User user = new User(null, "test@ya.ru", "login", "name",
                LocalDate.now().plusDays(1));
        InvalidDateException ex = assertThrows(InvalidDateException.class,
                () -> userService.create(user));
        assertEquals("Birthday is after current date", ex.getMessage());
    }

    @Test
    void createUserWithBirthdayExactlyNowShouldBeAllowed() {
        User user = new User(null, "test@ya.ru", "login", "name", LocalDate.now());
        assertDoesNotThrow(() -> userService.create(user));
    }

    @Test
    void createUserWithNullBirthdayShouldThrowValidationException() {
        User user = new User(null, "test@ya.ru", "login", "name", null);
        ValidationException ex = assertThrows(ValidationException.class,
                () -> userService.create(user));
        assertEquals("Birthday is required", ex.getMessage());
    }

    @Test
    void createDuplicateUserByEmailAndLoginShouldThrowDuplicatedDataException() {
        User first = new User(null, "dup@ya.ru", "dup", "name",
                LocalDate.now().minusDays(365));
        userService.create(first);
        User second = new User(null, "dup@ya.ru", "dup", "other",
                LocalDate.now().minusDays(100));
        DuplicatedDataException ex = assertThrows(DuplicatedDataException.class,
                () -> userService.create(second));
        assertEquals("User already exists", ex.getMessage());
    }

    @Test
    void updateExistingUserShouldChangeFields() {
        User original = userService.create(
                new User(null, "old@ya.ru", "oldLogin", "OldName",
                        LocalDate.now().minusDays(365)));
        User update = new User(original.getId(), "new@ya.ru", "newLogin", "NewName",
                LocalDate.now().minusDays(365));
        User result = userService.update(update);
        assertEquals("new@ya.ru", result.getEmail());
        assertEquals("newLogin", result.getLogin());
        assertEquals("NewName", result.getName());
    }

    @Test
    void updateUserWithNullIdShouldThrowValidationException() {
        User user = new User(null, "a@b.com", "login", "name", LocalDate.now());
        ValidationException ex = assertThrows(ValidationException.class,
                () -> userService.update(user));
        assertEquals("User id is null", ex.getMessage());
    }

    @Test
    void updateNonExistentUserShouldThrowNotFoundException() {
        User user = new User(999L, "a@b.com", "login", "name", LocalDate.now());
        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> userService.update(user));
        assertEquals("User with id 999 not found", ex.getMessage());
    }

    @Test
    void updateUserWithDuplicateEmailShouldThrowException() {
        User first = userService.create(
                new User(null, "first@ya.ru", "firstLogin", "First",
                        LocalDate.now().minusDays(365)));
        User second = userService.create(
                new User(null, "second@ya.ru", "secondLogin", "Second",
                        LocalDate.now().minusDays(365)));
        User update = new User(second.getId(), "first@ya.ru", "firstLogin", "irrelevant",
                LocalDate.now().minusDays(365));
        DuplicatedDataException ex = assertThrows(DuplicatedDataException.class,
                () -> userService.update(update));
        assertEquals("Email already in use", ex.getMessage());
    }

    @Test
    void updateUserWithSameEmailDifferentCaseShouldNotTriggerEmailChange() {
        User original = userService.create(
                new User(null, "user@ya.ru", "login", "Name",
                        LocalDate.now().minusDays(365)));
        User update = new User(original.getId(), "USER@ya.ru", "newLogin", "NewName",
                LocalDate.now().minusDays(365));
        User result = userService.update(update);
        assertEquals("user@ya.ru", result.getEmail());
        assertEquals("newLogin", result.getLogin());
        assertEquals("NewName", result.getName());
    }

    @Test
    void updateUserWithNullEmailShouldKeepOldEmail() {
        User original = userService.create(
                new User(null, "user@ya.ru", "login", "Name",
                        LocalDate.now().minusDays(365)));
        User update = new User(original.getId(), null, "newLogin", "NewName",
                LocalDate.now().minusDays(365));
        User result = userService.update(update);
        assertEquals("user@ya.ru", result.getEmail());
        assertEquals("NewName", result.getName());
    }

    @Test
    void updateUserWithBlankLoginShouldKeepOldLogin() {
        User original = userService.create(
                new User(null, "user@ya.ru", "oldLogin", "Name",
                        LocalDate.now().minusDays(365)));
        User update = new User(original.getId(), null, "   ", "NewName",
                LocalDate.now().minusDays(365));
        User result = userService.update(update);
        assertEquals("oldLogin", result.getLogin());
    }

    @Test
    void updateUserWithNullNameShouldKeepOldName() {
        User original = userService.create(
                new User(null, "user@ya.ru", "login", "OldName",
                        LocalDate.now().minusDays(365)));
        User update = new User(original.getId(), null, null, null,
                LocalDate.now().minusDays(365));
        User result = userService.update(update);
        assertEquals("OldName", result.getName());
    }

    @Test
    void updateUserWithBlankNameShouldKeepOldName() {
        User original = userService.create(
                new User(null, "user@ya.ru", "login", "OldName",
                        LocalDate.now().minusDays(365)));
        User update = new User(original.getId(), null, null, "   ",
                LocalDate.now().minusDays(365));
        User result = userService.update(update);
        assertEquals("OldName", result.getName());
    }
}