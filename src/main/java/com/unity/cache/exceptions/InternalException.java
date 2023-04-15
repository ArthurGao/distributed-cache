package com.unity.cache.exceptions;

/**
 *  InternalException is used to wrap any exception that is not expected to be thrown.
 */
public class InternalException extends RuntimeException{
    /**
     * Constructor
     * @param message error message
     */
    public InternalException(String message) {
        super(message);
    }

    /**
     * Constructor
     * @param message error message
     * @param cause the cause
     */
    public InternalException(String message, Throwable cause) {
        super(message, cause);
    }
}
