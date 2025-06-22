package com.coditas.tool.management.system.exception;

public class UserEmailAlreadyExistsException extends RuntimeException{

    public UserEmailAlreadyExistsException() {
    }

    public UserEmailAlreadyExistsException(String message) {
        super(message);
    }

    public UserEmailAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserEmailAlreadyExistsException(Throwable cause) {
        super(cause);
    }

    public UserEmailAlreadyExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}