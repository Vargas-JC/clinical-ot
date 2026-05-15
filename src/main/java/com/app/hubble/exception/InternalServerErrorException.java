package com.app.hubble.exception;

public class InternalServerErrorException extends ApiException{
    public InternalServerErrorException(String message) {
        super(message);
    }
}
