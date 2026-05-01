package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@EqualsAndHashCode(of = {"email", "login"})
@AllArgsConstructor
@Data
public class User {

    public interface Create {}
    public interface Update {}

    @NotNull(groups = Update.class, message = "User id must not be null")
    private Long id;

    @NotBlank(groups = Create.class, message = "Email is required")
    @Email(groups = {Create.class, Update.class}, message = "Email must be valid")
    private String email;

    @NotBlank(groups = Create.class, message = "Login is required")
    private String login;

    private String name;

    @NotNull(groups = Create.class, message = "Birthday is required")
    @PastOrPresent(groups = {Create.class, Update.class}, message = "Birthday must be in the past or today")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate birthday;
}