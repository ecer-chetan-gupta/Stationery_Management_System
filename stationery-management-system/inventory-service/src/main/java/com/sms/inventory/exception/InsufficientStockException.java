package com.sms.inventory.exception;

/**
 * Thrown when deducting more stock than is available.
 */
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }
}
