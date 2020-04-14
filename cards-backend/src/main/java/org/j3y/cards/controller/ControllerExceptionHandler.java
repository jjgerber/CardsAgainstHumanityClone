package org.j3y.cards.controller;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.j3y.cards.exception.CardsException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = { HttpStatusCodeException.class })
    protected ResponseEntity<Object> handleConflict(
            HttpStatusCodeException ex, WebRequest request) {
        ObjectNode responseJson = JsonNodeFactory.instance.objectNode();
        responseJson.put("error", ex.getStatusText());
        responseJson.put("code", ex.getRawStatusCode());
        return handleExceptionInternal(ex, responseJson, new HttpHeaders(), ex.getStatusCode(), request);
    }

    @ExceptionHandler(value = { CardsException.class })
    protected ResponseEntity<Object> handleConflict(
            CardsException ex, WebRequest request) {
        ObjectNode responseJson = JsonNodeFactory.instance.objectNode();
        responseJson.put("message", ex.getMessage());
        responseJson.put("code", ex.getStatus().value());
        return handleExceptionInternal(ex, responseJson, new HttpHeaders(), ex.getStatus(), request);
    }

}