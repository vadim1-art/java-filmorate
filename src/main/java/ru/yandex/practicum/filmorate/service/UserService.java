package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;


    public void addFriend(Long userId, Long friendId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Friend not found"));
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        User friend = userStorage.findById(friendId)
                .orElseThrow(() -> new NotFoundException("Friend not found"));
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public Collection<User> getFriends(Long userId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return user.getFriends().stream()
                .map(friendId -> userStorage.findById(friendId)
                        .orElseThrow(() -> new NotFoundException("Friend with id " + friendId + " not found")))
                .collect(Collectors.toList());
    }

    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        User user = userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id " + userId + " not found"));
        User otherUser = userStorage.findById(otherUserId)
                .orElseThrow(() -> new NotFoundException("User with id " + otherUserId + " not found"));

        Set<Long> commonIds = new HashSet<>(user.getFriends());
        commonIds.retainAll(otherUser.getFriends());  // только общие ID

        return commonIds.stream()
                .map(id -> userStorage.findById(id)
                        .orElseThrow(() -> new NotFoundException("Friend with id " + id + " not found")))
                .collect(Collectors.toList());
    }

    public Collection<User> getUsers() {
        return userStorage.findAll();
    }

    public User update(User updatedUser) {
        User oldUser = userStorage.findById(updatedUser.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (oldUser == null) {
            throw new NotFoundException("User with id " + updatedUser.getId() + " not found");
        }

        if (updatedUser.getEmail() != null && !updatedUser.getEmail().equalsIgnoreCase(oldUser.getEmail())) {
            boolean emailExists = userStorage.findAll().stream()
                    .anyMatch(f -> f.getName().equalsIgnoreCase(updatedUser.getEmail()));
            if (emailExists) {
                throw new DuplicatedDataException("Email already in use");
            }
            oldUser.setEmail(updatedUser.getEmail());
        }

        if (updatedUser.getLogin() != null && !updatedUser.getLogin().isBlank()) {
            oldUser.setLogin(updatedUser.getLogin());
        }
        if (updatedUser.getName() != null && !updatedUser.getName().isBlank()) {
            oldUser.setName(updatedUser.getName());
        }
        if (updatedUser.getBirthday() != null) {
            oldUser.setBirthday(updatedUser.getBirthday());
        }
        return userStorage.update(oldUser);
    }

    public User create(User user) {
        boolean emailExists = userStorage.findAll().stream()
                .anyMatch(f -> f.getName().equalsIgnoreCase(user.getEmail()));
        if (emailExists) {
            throw new DuplicatedDataException("Email already in use");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        user.setId(getNextId());
        userStorage.save(user);
        log.info("Created user: {}", user);
        return user;
    }

    private long getNextId() {
        return userStorage.getKey().stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0) + 1;
    }
}