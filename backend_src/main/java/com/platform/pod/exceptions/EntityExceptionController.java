package com.platform.pod.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class EntityExceptionController {
    @ExceptionHandler(value = ApiException.class)
    public ResponseEntity<GenericErrorResponseTemplate> handleApiException(ApiException e) {
        GenericErrorResponseTemplate errorResponseTemplate = new GenericErrorResponseTemplate();
        errorResponseTemplate.setStatus(e.getStatus().value());
        errorResponseTemplate.setMessage(e.getMessage());
        errorResponseTemplate.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(errorResponseTemplate, e.getStatus());
    }

    @ExceptionHandler(value = NotOrganizerException.class)
    public ResponseEntity<GenericErrorResponseTemplate> handleNotOrganizerException(NotOrganizerException e) {
        GenericErrorResponseTemplate errorResponseTemplate = new GenericErrorResponseTemplate();
        errorResponseTemplate.setStatus(400);
        errorResponseTemplate.setMessage(e.getMessage());
        errorResponseTemplate.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(errorResponseTemplate, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(value = HttpMessageNotReadableException.class)
    public ResponseEntity<GenericErrorResponseTemplate> handleNotOrganizerException(HttpMessageNotReadableException e) {
        GenericErrorResponseTemplate errorResponseTemplate = new GenericErrorResponseTemplate();
        errorResponseTemplate.setStatus(400);
        errorResponseTemplate.setMessage(e.getMessage());
        errorResponseTemplate.setTimeStamp(String.valueOf(System.currentTimeMillis()));
        return new ResponseEntity<>(errorResponseTemplate, HttpStatus.BAD_REQUEST);
    }
}
