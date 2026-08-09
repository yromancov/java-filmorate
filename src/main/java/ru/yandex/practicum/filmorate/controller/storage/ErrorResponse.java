package ru.yandex.practicum.filmorate.controller.storage;

public class ErrorResponse {

    String error;


    public ErrorResponse(String error) {
        this.error = error;

    }

    public String getError() {
        return error;
    }


}