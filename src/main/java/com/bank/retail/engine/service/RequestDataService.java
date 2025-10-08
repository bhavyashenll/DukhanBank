package com.bank.retail.engine.service;

import com.bank.retail.api.dto.XmlConversionRequest;

import java.util.Map;

public interface RequestDataService {
    Map<String, Object> prepareRequestData(XmlConversionRequest request, String serviceName);
}
