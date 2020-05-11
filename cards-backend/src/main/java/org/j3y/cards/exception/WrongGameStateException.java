package org.j3y.cards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Game is not in the right state for that action.")
public class WrongGameStateException extends CardsException {

    private final HttpStatus status = HttpStatus.BAD_REQUEST;

    public WrongGameStateException() {
        super();
    }

    public WrongGameStateException(String message) {
        super(message);
    }

    public WrongGameStateException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
