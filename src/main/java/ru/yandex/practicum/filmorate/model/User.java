package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.validation.Create;
import ru.yandex.practicum.filmorate.validation.Update;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@EqualsAndHashCode(of = {"email", "login"})
@Data
public class User {

    @NotNull(groups = Update.class, message = "User id must not be null")
    private Long id;

    @NotBlank(groups = Create.class, message = "Email is required")
    @Email(groups = {Create.class, Update.class}, message = "Email must be valid")
    private String email;

    @Pattern(
            regexp = "^[^<>&\"']+$",
            groups = {Create.class, Update.class},
            message = "Invalid characters"
    )
    @NotBlank(groups = Create.class, message = "Login is required")
    private String login;

    @Pattern(
            regexp = "^[^<>&\"']+$",
            groups = {Create.class, Update.class},
            message = "Invalid characters"
    )
    private String name;

    @NotNull(groups = Create.class, message = "Birthday is required")
    @PastOrPresent(groups = {Create.class, Update.class}, message = "Birthday must be in the past or today")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthday;

    @JsonIgnore
    private Set<Long> friends = new HashSet<>();

    public User() {
        this.friends = new HashSet<>();
    }

    public User(Long id, String email, String login, String name, LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
        this.friends = new HashSet<>();
    }

    public User(Long id, String email, String login, String name, LocalDate birthday, Set<Long> friends) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
        this.friends = friends != null ? friends : new HashSet<>();
    }
}