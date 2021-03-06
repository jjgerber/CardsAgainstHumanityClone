package org.j3y.cards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Player attempted an invalid action.")
public class InvalidActionException extends CardsException {

    private final HttpStatus status = HttpStatus.BAD_REQUEST;

    public InvalidActionException() {
        super();
    }

    public InvalidActionException(String message) {
        super(message);
    }

    public InvalidActionException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
