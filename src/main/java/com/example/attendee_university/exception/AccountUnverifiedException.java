package com.example.attendee_university.exception;

public class AccountUnverifiedException extends BadRequestException {

    private final String email;

    public AccountUnverifiedException(String email) {
        super("Please verify your email before logging in.");
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
