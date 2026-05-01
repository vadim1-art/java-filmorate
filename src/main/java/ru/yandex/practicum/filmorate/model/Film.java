package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@AllArgsConstructor
@Data
public class Film {

    public interface Create {}

    public interface Update {}

    @NotNull(groups = Update.class, message = "Film id must not be null")
    private Long id;

    @NotBlank(groups = Create.class, message = "Film name cannot be empty")
    private String name;

    @Size(max = 200, message = "Film description is too long", groups = {Create.class, Update.class})
    private String description;

    @NotNull(groups = Create.class, message = "Release date is required")
    private LocalDate releaseDate;

    @NotNull(groups = Create.class, message = "Duration is required")
    @Positive(groups = {Create.class, Update.class}, message = "Duration must be positive")
    private Long duration;

    @AssertTrue(groups = {Create.class, Update.class}, message = "Release date must not be before 28.12.1895")
    private boolean isReleaseDateValid() {
        if (releaseDate == null) return true;
        return !releaseDate.isBefore(LocalDate.of(1895, 12, 28));
    }
}