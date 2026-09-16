package com.manish.b2bmonitor.as2;

public class InvalidAs2RequestException extends RuntimeException {
    public InvalidAs2RequestException(String message) {
        super(message);
    }
}
