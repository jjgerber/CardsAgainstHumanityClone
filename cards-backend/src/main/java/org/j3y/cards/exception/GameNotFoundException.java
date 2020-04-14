package org.j3y.cards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "That game was not found.")
public class GameNotFoundException extends CardsException {

    private final HttpStatus status = HttpStatus.NOT_FOUND;

    public GameNotFoundException() {
        super();
    }

    public GameNotFoundException(String message) {
        super(message);
    }

    public GameNotFoundException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
