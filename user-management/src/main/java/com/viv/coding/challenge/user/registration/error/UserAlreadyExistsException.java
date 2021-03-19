package com.viv.coding.challenge.user.registration.error;

/**
 * Custom exception class to indicate that User Already Exists
 * 
 * @Author Vivek Rao
 */
public final class UserAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = -121858590999113771L;

    public UserAlreadyExistsException(final String message) {
        super(message);
    }

}
