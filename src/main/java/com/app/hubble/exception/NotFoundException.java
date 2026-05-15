package com.app.hubble.exception;

public class NotFoundException extends ApiException{
    public NotFoundException(String message) {
        super(message);
    }
}
