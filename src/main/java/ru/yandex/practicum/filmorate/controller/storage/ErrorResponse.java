package ru.yandex.practicum.filmorate.controller.storage;

public class ErrorResponse {
    // название ошибки
    String error;
    // подробное описание

    public ErrorResponse(String error) {
        this.error = error;

    }

    // геттеры необходимы, чтобы Spring Boot мог получить значения полей
    public String getError() {
        return error;
    }


}