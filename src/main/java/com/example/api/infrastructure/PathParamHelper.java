package com.example.api.infrastructure;

import com.example.api.dto.BaseServiceRequest;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Helper class for handling path parameter operations
 */
@Component
public class PathParamHelper {

    /**
     * Creates a new BaseServiceRequest with a path parameter added/overridden
     * @param originalRequest The original request
     * @param paramName The parameter name to add/override (can be null if no path param)
     * @param paramValue The parameter value from path variable (can be null if no path param)
     * @return Updated BaseServiceRequest (always returns a new instance)
     */
    public BaseServiceRequest createRequestWithPathParam(BaseServiceRequest originalRequest, String paramName, String paramValue) {
        Map<String, Object> requestInfo = originalRequest.getRequestInfo();
        Map<String, Object> updatedData = Objects.nonNull(requestInfo) ? new HashMap<>(requestInfo) : new HashMap<>();

        // Only add path parameter if both paramName and paramValue are provided and valid
        if (Objects.nonNull(paramName) && Objects.nonNull(paramValue) && !paramValue.isBlank()) {
            updatedData.put(paramName, paramValue);
        }

        return new BaseServiceRequest(updatedData, originalRequest.getDeviceInfo());
    }
}
