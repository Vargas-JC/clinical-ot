package com.app.hubble.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}
