package com.example.api.exception;

import org.springframework.http.HttpStatus;

/**
 * Custom API Exception for handling API-specific errors
 * Includes error code, message, and HTTP status for standardized error responses
 */
public class CustomApiException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus httpStatus;

    public CustomApiException(String errorCode, String message, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public CustomApiException(String errorCode, String message, HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
