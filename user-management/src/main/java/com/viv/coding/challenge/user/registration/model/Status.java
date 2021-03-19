package com.viv.coding.challenge.user.registration.model;

/**
 * Enum class containing possible Status messages that are sent as part of Response.
 * 
 * @Author Vivek Rao
 */
public enum Status {
    SUCCESS("Request is Successful"),
    LOGIN_FAILED("Login failure. Please create the user before logging in!"),
    LOGIN_SUCCESSFUL("Login successful."),
    USER_ALREADY_EXISTS("User already exists!! Please enter a different e-mail address or username."),
    USER_NOT_FOUND("User does not exist!! Please check the user details entered.");

    private final String status;

    Status(final String message) {
        status = message;
    }

    @Override
    public String toString() {
        return "Status: " + status;
    }
}
