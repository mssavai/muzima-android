package com.muzima.utils.smartcard;

public class SmartCardIntentResult {
    private String message;
    private Throwable errors;

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setErrors(Throwable errors) {
        this.errors = errors;
    }

    public Throwable getErrors() {
        return errors;
    }
}
