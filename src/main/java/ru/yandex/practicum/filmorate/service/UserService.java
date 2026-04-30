package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.InvalidDateException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserService {
    private final Map<Long, User> users = new HashMap<>();

    public Collection<User> getUsers() {
        return users.values();
    }

    public User update(User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("User id is null");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (newUser.getEmail() != null && !newUser.getEmail().equalsIgnoreCase(oldUser.getEmail())) {
                boolean emailExists = users.values().stream()
                        .anyMatch(u -> u.getEmail().equalsIgnoreCase(newUser.getEmail()));
                if (emailExists) {
                    throw new DuplicatedDataException("Email already in use");
                }
                oldUser.setEmail(newUser.getEmail());
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
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new ValidationException("Email is required");
        }
        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Email must contain '@'");
        }
        if (user.getLogin() == null || user.getLogin().isBlank()) {
            throw new ValidationException("Login is required");
        }
        if (user.getBirthday() == null) {
            throw new ValidationException("Birthday is required");
        }
        if (users.containsValue(user)) {
            log.warn("Попытка создать дубликат пользователя: {}", user);
            throw new DuplicatedDataException("User already exists");
        }
        if (user.getBirthday().isAfter(Instant.now())) {
            log.warn("Дата рождения пользователя в будущем: {}", user.getBirthday());
            throw new InvalidDateException("Birthday is after current time");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
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
        return ++currentMaxId;
    }
}