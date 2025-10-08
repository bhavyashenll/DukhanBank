package com.bank.retail.engine.service;

import java.util.Map;

public interface XmlHeaderService {
    Map<String, Object> getHeadersForService(String serviceName) throws Exception;
}
