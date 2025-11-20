package com.example.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standardized API Response Wrapper
 * Format: { "data": {...}, "status": { "code": "...", "description": "..." } }
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse<T> {

    private T data;
    private ResponseStatus status;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResponseStatus {
        private String code;
        private String description;
    }

    /**
     * Create success response with code 000000
     */
    public static <T> BaseResponse<T> success(T data) {
        return BaseResponse.<T>builder()
                .data(data)
                .status(ResponseStatus.builder()
                        .code("000000")
                        .description("SUCCESS")
                        .build())
                .build();
    }

    /**
     * Create success response with custom message
     */
    public static <T> BaseResponse<T> success(T data, String description) {
        return BaseResponse.<T>builder()
                .data(data)
                .status(ResponseStatus.builder()
                        .code("000000")
                        .description(description)
                        .build())
                .build();
    }

    /**
     * Create error response with specific error code
     */
    public static <T> BaseResponse<T> error(String code, String description) {
        return BaseResponse.<T>builder()
                .data(null)
                .status(ResponseStatus.builder()
                        .code(code)
                        .description(description)
                        .build())
                .build();
    }

    /**
     * Create error response (defaults to 000500 - Internal Server Error)
     */
    public static <T> BaseResponse<T> error(String description) {
        return error("000500", description);
    }

    /**
     * Create bad request error (000400)
     */
    public static <T> BaseResponse<T> badRequest(String description) {
        return error("000400", description);
    }

    /**
     * Create not found error (000404)
     */
    public static <T> BaseResponse<T> notFound(String description) {
        return error("000404", description);
    }

    /**
     * Create request timeout error (000408)
     */
    public static <T> BaseResponse<T> timeout(String description) {
        return error("000408", description);
    }

    /**
     * Create service unavailable error (000503)
     */
    public static <T> BaseResponse<T> serviceUnavailable(String description) {
        return error("000503", description);
    }
}
