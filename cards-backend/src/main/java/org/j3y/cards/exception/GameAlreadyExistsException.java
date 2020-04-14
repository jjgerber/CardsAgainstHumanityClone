package org.j3y.cards.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT, reason = "Game with that name already exists.")
public class GameAlreadyExistsException extends CardsException {

    private final HttpStatus status = HttpStatus.CONFLICT;

    public GameAlreadyExistsException() {
        super();
    }

    public GameAlreadyExistsException(String message) {
        super(message);
    }

    public GameAlreadyExistsException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public HttpStatus getStatus() {
        return status;
    }
}
