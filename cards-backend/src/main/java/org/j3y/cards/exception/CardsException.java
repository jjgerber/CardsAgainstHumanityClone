package org.j3y.cards.exception;

import org.springframework.http.HttpStatus;

public class CardsException extends RuntimeException {

    private final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;

    public CardsException() {
        super();
    }

    public CardsException(String message) {
        super(message);
    }

    public CardsException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
