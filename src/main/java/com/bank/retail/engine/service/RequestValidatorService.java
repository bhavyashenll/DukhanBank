package com.bank.retail.engine.service;

import com.bank.retail.api.dto.XmlConversionRequest;

public interface RequestValidatorService {
    void validateRequest(String serviceName, XmlConversionRequest request) throws Exception;
}
