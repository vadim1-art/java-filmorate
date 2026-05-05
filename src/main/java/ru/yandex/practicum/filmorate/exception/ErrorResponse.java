package ru.yandex.practicum.filmorate.exception;

import java.time.LocalDateTime;

public record ErrorResponse(String error, int status, LocalDateTime timestamp) {
    public ErrorResponse(String error, int status) {
        this(error, status, LocalDateTime.now());
    }
}