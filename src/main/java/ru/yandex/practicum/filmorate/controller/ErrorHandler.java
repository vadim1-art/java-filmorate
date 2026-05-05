package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
//import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.filmorate.exception.ErrorResponse;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

@RestControllerAdvice
public class ErrorHandler {

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
    public ErrorResponse handleValidation(ValidationException e) {
        return new ErrorResponse(e.getMessage(), HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND) // 404
    public ErrorResponse handleNotFound(NotFoundException e) {
        return new ErrorResponse(e.getMessage(), HttpStatus.NOT_FOUND.value());
    }

//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    @ResponseStatus(HttpStatus.BAD_REQUEST) // 400
//    public ErrorResponse handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
//        String message = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
//        return new ErrorResponse(message != null ? message : "Validation failed");
//    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR) // 500
    public ErrorResponse handleOther(Exception e) {
        return new ErrorResponse(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}