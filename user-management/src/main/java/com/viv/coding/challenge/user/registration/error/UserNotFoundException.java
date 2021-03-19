package com.viv.coding.challenge.user.registration.error;

/**
 * Custom class for User Not found exception
 * 
 * @Author Vivek Rao
 */
public final class UserNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 26674999199888780L;

    public UserNotFoundException(final String message) {
        super(message);
    }

}
