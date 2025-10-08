package com.bank.retail.engine.service;

import com.bank.retail.api.dto.XmlConversionRequest;

import java.util.Map;

public interface XmlGeneratorService {
    String generateXmlFromXsd(String serviceName, XmlConversionRequest request, Map<String, Object> headers) throws Exception;
}
