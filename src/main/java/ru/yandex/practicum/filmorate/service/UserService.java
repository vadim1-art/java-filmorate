package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    @Qualifier("userDbStorage")
    private final UserStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("user_id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    }

    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.save(user);
    }

    public User update(User user) {
        getUserById(user.getId());
        return userStorage.update(user);
    }

    public Collection<User> getUsers() {
        return userStorage.findAll();
    }

    public User getUserById(Long id) {
        return userStorage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    public void addFriend(Long userId, Long friendId) {
        getUserById(userId);
        getUserById(friendId);

        String sql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
        log.info("Пользователь {} добавил в друзья {}", userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        getUserById(userId);
        getUserById(friendId);

        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, userId, friendId);

        if (rowsAffected > 0) {
            log.info("Пользователь {} удалил из друзей {}", userId, friendId);
        } else {
            log.info("Связи между {} и {} не было, удалять нечего", userId, friendId);
        }
    }

    public void removeFriend(Long userId, Long friendId) {
        getUserById(userId);
        getUserById(friendId);

        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, userId, friendId);

        if (rowsAffected > 0) {
            log.info("Пользователь {} успешно удалил из друзей {}", userId, friendId);
        }
    }

    public Collection<User> getFriends(Long userId) {
        getUserById(userId);

        String sql = "SELECT u.* FROM users u " +
                "JOIN friends f ON u.user_id = f.friend_id " +
                "WHERE f.user_id = ?";

        return jdbcTemplate.query(sql, this::mapRowToUser, userId);
    }

    public Collection<User> getCommonFriends(Long userId, Long otherId) {
        getUserById(userId);
        getUserById(otherId);

        String sql = "SELECT u.* FROM users u " +
                "JOIN friends f1 ON u.user_id = f1.friend_id " +
                "JOIN friends f2 ON u.user_id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";

        return jdbcTemplate.query(sql, this::mapRowToUser, userId, otherId);
    }
}