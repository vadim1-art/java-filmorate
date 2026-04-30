package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.InvalidDateException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserService {
    private final Map<Long, User> users = new HashMap<>();
    private LocalDate today = LocalDate.now();

    public Collection<User> getUsers() {
        return users.values();
    }

    public User update(User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("Film id is null");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (newUser.getEmail() != null &&
                    !newUser.getEmail().equalsIgnoreCase(oldUser.getEmail())) {
                if (users.containsValue(newUser)) {
                    throw new DuplicatedDataException("Email already in use");
                }
                oldUser.setName(newUser.getEmail());
            }
            if (newUser.getLogin() != null && !newUser.getLogin().isBlank()) {
                oldUser.setLogin(newUser.getLogin());
            }
            if (newUser.getName() != null && !newUser.getName().isBlank()) {
                oldUser.setName(newUser.getName());
            }
            return oldUser;
        }
        throw new NotFoundException("User with id " + newUser.getId() + " not found");
    }

    public User create(User user) {
        if (users.containsValue(user)) {
            log.warn("Попытка создать дубликат пользователя: {}", user);
            throw new DuplicatedDataException("User already exists");
        }
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.warn("Попытка создать пользователя без email");
            throw new ValidationException("Email is required");
        }
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Email must contain '@'");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            log.warn("Попытка создать пользователя с пустым логином");
            throw new ValidationException("Login is required");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        if (user.getBirthday().isAfter(Instant.from(today))) {
            log.warn("Дата рождения пользователя в будущем: {}", user.getBirthday());
            throw new InvalidDateException("Birthday is after today");
        }

        user.setId(getNextId());
        users.put(user.getId(), user);
        return user;
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        long newId = ++currentMaxId;
        return newId;
    }
}