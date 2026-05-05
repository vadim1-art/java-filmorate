package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.validation.Create;
import ru.yandex.practicum.filmorate.validation.Update;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@AllArgsConstructor
@EqualsAndHashCode(of = {"email", "login"})
@Builder
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
    @Builder.Default
    private Set<Long> friends = new HashSet<>();
}