package com.hackathon.payment.common.exception;

/**
 * Thrown when an Idempotency-Key is reused with a different request payload.
 */
public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException(String message) {
        super(message);
    }
}
