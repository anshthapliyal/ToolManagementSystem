package com.coditas.tool.management.system.exception;

public class PremiseNotFoundException extends RuntimeException{
    public PremiseNotFoundException() {
    }

    public PremiseNotFoundException(String message) {
        super(message);
    }

    public PremiseNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public PremiseNotFoundException(Throwable cause) {
        super(cause);
    }

    public PremiseNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
