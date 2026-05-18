package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    User save(User user);

    User update(User user);

    Optional<User> findById(Long id);

    Collection<User> findAll();

    Collection<Long> getKey();

}
