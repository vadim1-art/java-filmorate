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
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserServiceTest {

    private final UserService userService;

    @Test
    void createValidUserShouldReturnUserWithId() {
        User user = new User(null, "test@ya.ru",
                "login", "name", LocalDate.now().minusYears(20));
        User created = userService.create(user);

        assertNotNull(created.getId());
        assertEquals(1L, created.getId());
    }

    @Test
    void createUserWithEmptyNameShouldUseLoginAsName() {
        User user = new User(null, "test@mail.ru",
                "MyLogin", "", LocalDate.now().minusYears(20));
        User created = userService.create(user);

        assertEquals("MyLogin", created.getName());
    }

    @Test
    void addFriendShouldWorkOneWay() {
        User user1 = userService.create(new User(null, "u1@ya.ru",
                "l1", "n1", LocalDate.now().minusYears(20)));
        User user2 = userService.create(new User(null, "u2@ya.ru",
                "l2", "n2", LocalDate.now().minusYears(20)));

        userService.addFriend(user1.getId(), user2.getId());

        Collection<User> friendsOf1 = userService.getFriends(user1.getId());
        Collection<User> friendsOf2 = userService.getFriends(user2.getId());

        assertEquals(1, friendsOf1.size(),
                "У первого пользователя должен быть 1 друг");
        assertEquals(0, friendsOf2.size(),
                "У второго пользователя должно быть 0 друзей (односторонняя связь)");
    }

    @Test
    void updateExistingUserShouldChangeFields() {
        User original = userService.create(new User(null, "old@ya.ru",
                "old", "Old", LocalDate.now().minusYears(20)));
        User update = new User(original.getId(), "new@ya.ru",
                "new", "New", LocalDate.now().minusYears(20));

        User result = userService.update(update);

        assertEquals("new@ya.ru", result.getEmail());
        assertEquals("New", result.getName());
    }

    @Test
    void updateNonExistentUserShouldThrowNotFoundException() {
        User user = new User(999L, "a@b.com", "login", "name", LocalDate.now());
        assertThrows(NotFoundException.class, () -> userService.update(user));
    }
}