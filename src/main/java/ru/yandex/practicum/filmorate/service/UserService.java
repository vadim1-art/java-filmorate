package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@RequiredArgsConstructor
@Slf4j
@Service
public class UserService {
    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    public void addFriend(Long userId, Long friendId) {
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        userStorage.findById(friendId).orElseThrow(() -> new NotFoundException("Friend not found"));

        String sql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    public Collection<User> getFriends(Long userId) {
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
        String sql = "SELECT u.* FROM users u JOIN friends f ON u.user_id = f.friend_id WHERE f.user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getLong("user_id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        }, userId);
    }

    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friends f1 ON u.user_id = f1.friend_id " +
                "JOIN friends f2 ON u.user_id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            User user = new User();
            user.setId(rs.getLong("user_id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        }, userId, otherUserId);
    }

    public Collection<User> getUsers() {
        return userStorage.findAll();
    }

    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        userStorage.save(user);
        return user;
    }

    public User update(User user) {
        userStorage.findById(user.getId()).orElseThrow(() -> new NotFoundException("User not found"));
        return userStorage.update(user);
    }

    public User getUserById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }
}