package com.bardock.discordvoicebot.exception;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import tools.jackson.databind.exc.MismatchedInputException;

import java.net.URI;
import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleResourceNotFoundException(ResourceNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        problemDetail.setTitle("Resource not found");
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGenericException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected internal error has occurred.");
        problemDetail.setTitle("Internal server error");
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setProperty("timestamp", Instant.now());
        // problemDetail.setProperty("cause", ex.getMessage());

        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String paramName = ex.getPropertyName();
        String Expectedtype = ex.getRequiredType() != null ? ex.getRequiredType().getName() : "unknown";

        String detailMessage = String.format("the parameter '%s' received an invalid value. " +
                "Make sure you send an %s.", paramName, Expectedtype);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detailMessage);
        problemDetail.setTitle("Invalid parameter in the URL");
        problemDetail.setType(URI.create("about:blank"));
        problemDetail.setProperty("timestamp", Instant.now());

        return problemDetail;
    }

}
