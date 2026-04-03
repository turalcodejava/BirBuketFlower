package com.birbuket.authservice.exception;

public class UserIsNotActive extends RuntimeException {
    public UserIsNotActive(String message) {
        super(message);
    }
}
