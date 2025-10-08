package com.bank.retail.engine.service;

import java.util.List;
import java.util.Map;

public interface XmlParsingService {
    List<Map<String, Object>> parseXmlResponse(String xmlResponse) throws Exception;
}
