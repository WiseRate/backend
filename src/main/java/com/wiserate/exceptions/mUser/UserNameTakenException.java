package com.wiserate.exceptions.mUser;

public class UserNameTakenException extends RuntimeException {
    public UserNameTakenException(String message) {
        super(message);
    }
}
