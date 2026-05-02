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
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.validation.Create;
import ru.yandex.practicum.filmorate.validation.Update;

import java.time.LocalDate;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    private UserService userService;
    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

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
    void createUserWithBirthdayExactlyNowShouldBeAllowed() {
        User user = new User(null, "test@ya.ru", "login", "name", LocalDate.now());
        assertDoesNotThrow(() -> userService.create(user));
    }

    @Test
    void createDuplicateEmailShouldThrowException() {
        User first = new User(null, "dup@ya.ru", "dup", "name",
                LocalDate.now().minusDays(365));
        userService.create(first);
        User second = new User(null, "dup@ya.ru", "other", "otherName",
                LocalDate.now().minusDays(100));
        DuplicatedDataException ex = assertThrows(DuplicatedDataException.class,
                () -> userService.create(second));
        assertEquals("Email already in use", ex.getMessage());
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
        User update = new User(second.getId(), "first@ya.ru", "newLogin", "irrelevant",
                LocalDate.now().minusDays(365));
        DuplicatedDataException ex = assertThrows(DuplicatedDataException.class,
                () -> userService.update(update));
        assertEquals("Email already in use", ex.getMessage());
    }

    @Test
    void updateUserWithSameEmailDifferentCaseShouldNotChangeEmail() {
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

    @Test
    void updateUserWithNullBirthdayShouldKeepOldBirthday() {
        LocalDate bday = LocalDate.now().minusYears(20);
        User original = userService.create(
                new User(null, "user@ya.ru", "login", "Name", bday));
        User update = new User(original.getId(), null, null, null, null);
        User result = userService.update(update);
        assertEquals(bday, result.getBirthday());
    }

    @Test
    void userEmailMustNotBeBlank() {
        User user = new User(null, "   ", "login", "name", LocalDate.now());
        Set<ConstraintViolation<User>> violations = validator.validate(user, Create.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().contains("Email is required")));
    }

    @Test
    void userEmailMustBeValid() {
        User user = new User(null, "not-an-email", "login", "name", LocalDate.now());
        Set<ConstraintViolation<User>> violations = validator.validate(user, Create.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().contains("must be valid")));
    }

    @Test
    void userLoginMustNotBeBlank() {
        User user = new User(null, "a@b.com", "  ", "name", LocalDate.now());
        Set<ConstraintViolation<User>> violations = validator.validate(user, Create.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().contains("Login is required")));
    }

    @Test
    void userBirthdayMustNotBeInFuture() {
        User user = new User(null, "a@b.com", "login",
                "name", LocalDate.now().plusDays(1));
        Set<ConstraintViolation<User>> violations = validator.validate(user, Create.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().contains("Birthday must be in the past")));
    }

    @Test
    void userBirthdayMustNotBeNull() {
        User user = new User(null, "a@b.com", "login", "name", null);
        Set<ConstraintViolation<User>> violations = validator.validate(user, Create.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().contains("Birthday is required")));
    }

    @Test
    void updateUserIdMustNotBeNull() {
        User user = new User(null, "a@b.com", "login", "name", LocalDate.now());
        Set<ConstraintViolation<User>> violations = validator.validate(user, Update.class);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().contains("User id must not be null")));
    }
}