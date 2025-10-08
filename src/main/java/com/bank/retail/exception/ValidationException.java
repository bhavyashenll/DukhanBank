package com.bank.retail.exception;

import java.util.List;

public class ValidationException extends RuntimeException {
    
    private final List<String> missingFields;
    private final String errorCode;
    
    public ValidationException(String message) {
        super(message);
        this.missingFields = null;
        this.errorCode = null;
    }
    
    public ValidationException(String message, List<String> missingFields, String errorCode) {
        super(message);
        this.missingFields = missingFields;
        this.errorCode = errorCode;
    }
    
    public List<String> getMissingFields() {
        return missingFields;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}
