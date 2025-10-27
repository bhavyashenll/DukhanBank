package com.example.api.exception;

import com.example.api.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupportedException(HttpRequestMethodNotSupportedException ex) {
        ApiResponse<Object> response = ApiResponse.badRequest();
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }
    
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(Exception ex) {
        ApiResponse<Object> response = ApiResponse.noDataFound();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<ApiResponse<Object>> handleServletRequestBindingException(ServletRequestBindingException ex) {
        ApiResponse<Object> response = ApiResponse.mandatoryHeaderNotFound();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler({MethodArgumentNotValidException.class, MissingServletRequestParameterException.class, 
                      HttpMessageNotReadableException.class, BindException.class})
    public ResponseEntity<ApiResponse<Object>> handleBadRequestException(Exception ex) {
        ApiResponse<Object> response = ApiResponse.badRequest();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        ApiResponse<Object> response = ApiResponse.error();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
