package com.bank.retail.exception;

import com.bank.retail.api.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(ValidationException ex) {
        ApiResponse<Object> response = new ApiResponse<>(
            new ApiResponse.Status("000400", "Validation Error"), 
            List.of()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        ApiResponse<Object> response = ApiResponse.noDataFound();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Object>> handleBusinessException(BusinessException ex) {
        ApiResponse<Object> response = ApiResponse.error();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
    
    @ExceptionHandler({IllegalArgumentException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiResponse<Object>> handleIllegalArgumentException(Exception ex) {
        ApiResponse<Object> response = ApiResponse.noDataFound();
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
    
    @ExceptionHandler({MethodArgumentNotValidException.class, MissingServletRequestParameterException.class, 
                      HttpMessageNotReadableException.class, BindException.class})
    public ResponseEntity<ApiResponse<Object>> handleBadRequestException(Exception ex) {
        ApiResponse<Object> response = new ApiResponse<>(
            new ApiResponse.Status("000400", "Bad Request"), 
            List.of()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGenericException(Exception ex) {
        ApiResponse<Object> response = ApiResponse.error();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
