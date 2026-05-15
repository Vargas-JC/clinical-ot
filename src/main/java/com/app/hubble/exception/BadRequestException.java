package com.app.hubble.exception;

public class BadRequestException extends ApiException{
    public BadRequestException(String message) {
        super(message);
    }
}
