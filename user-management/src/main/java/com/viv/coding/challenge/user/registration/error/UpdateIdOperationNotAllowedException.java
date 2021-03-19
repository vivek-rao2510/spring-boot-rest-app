package com.viv.coding.challenge.user.registration.error;

/**
 * Exception class thrown if user ID from Request body doesn't match the path variable ID
 */
public class UpdateIdOperationNotAllowedException extends RuntimeException {

    private static final long serialVersionUID = -12111177843393771L;

    public UpdateIdOperationNotAllowedException(final String message) {
        super(message);
    }

}
