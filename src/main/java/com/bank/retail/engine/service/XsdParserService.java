package com.bank.retail.engine.service;

import org.w3c.dom.Document;

import java.util.List;

public interface XsdParserService {
    Document parseXsd(String serviceName) throws Exception;
    List<String> getMandatoryFieldsFromXsd(Document xsdDoc);
    List<String> getElementSequenceFromXsd(Document xsdDoc, String requestTypeName);
}
