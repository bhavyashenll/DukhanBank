package com.bank.retail.engine.service.impl;

import com.bank.retail.api.dto.XmlConversionRequest;
import com.bank.retail.engine.service.XmlGeneratorService;
import com.bank.retail.engine.service.RequestDataService;
import com.bank.retail.engine.service.XsdParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;

@Service
public class XmlGeneratorServiceImpl implements XmlGeneratorService {
    
    private static final Logger logger = LoggerFactory.getLogger(XmlGeneratorService.class);
    
    @Value("${app.security.userId}")
    private String securityUserId;
    
    @Value("${app.security.password}")
    private String securityPassword;
    
    private final RequestDataService requestDataService;
    private final XsdParserService xsdParserService;

    public XmlGeneratorServiceImpl(RequestDataService requestDataService, XsdParserService xsdParserService) {
        this.requestDataService = requestDataService;
        this.xsdParserService = xsdParserService;
    }
    
    public String generateXmlFromXsd(String serviceName, XmlConversionRequest request, Map<String, Object> headers) throws Exception {
        logger.debug("Generating XML for service: {}", serviceName);
        
        Map<String, Object> requestData = requestDataService.prepareRequestData(request, serviceName);
        
        Document xmlDoc = createXmlDocument();
        Element eaiMessage = createEaiMessage(xmlDoc);
        
        createEaiHeader(xmlDoc, eaiMessage, headers);
        createEaiBody(xmlDoc, eaiMessage, requestData, serviceName);
        
        String xmlResult = documentToString(xmlDoc);
        logger.debug("Generated XML length: {} characters", xmlResult.length());
        
        return xmlResult;
    }
    
    private Document createXmlDocument() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.newDocument();
    }
    
    private Element createEaiMessage(Document xmlDoc) {
        Element eaiMessage = xmlDoc.createElementNS("urn:esbbank.com/gbo/xml/schemas/v1_0/", "NS1:eAI_MESSAGE");
        eaiMessage.setAttribute("xmlns:NS1", "urn:esbbank.com/gbo/xml/schemas/v1_0/");
        xmlDoc.appendChild(eaiMessage);
        return eaiMessage;
    }
    
    private void createEaiHeader(Document xmlDoc, Element eaiMessage, Map<String, Object> headers) {
        Element eaiHeader = xmlDoc.createElementNS("urn:esbbank.com/gbo/xml/schemas/v1_0/", "NS1:eAI_HEADER");
        eaiMessage.appendChild(eaiHeader);
        
        addHeaderElement(xmlDoc, eaiHeader, "NS1:serviceName", headers.get("serviceName"));
        addHeaderElement(xmlDoc, eaiHeader, "NS1:serviceType", headers.get("serviceType"));
        addHeaderElement(xmlDoc, eaiHeader, "NS1:serviceVersion", headers.get("serviceVersion"));
        addHeaderElement(xmlDoc, eaiHeader, "NS1:client", headers.get("client"));
        addHeaderElement(xmlDoc, eaiHeader, "NS1:clientChannel", headers.get("clientChannel"));
        addHeaderElement(xmlDoc, eaiHeader, "NS1:msgChannel", headers.get("msgChannel"));
        addHeaderElement(xmlDoc, eaiHeader, "NS1:requestorLanguage", headers.get("requestorLanguage"));
        
        createSecurityInfo(xmlDoc, eaiHeader);
        addHeaderElement(xmlDoc, eaiHeader, "NS1:returnCode", headers.get("returnCode"));
    }
    
    private void createSecurityInfo(Document xmlDoc, Element eaiHeader) {
        Element securityInfo = xmlDoc.createElementNS("urn:esbbank.com/gbo/xml/schemas/v1_0/", "NS1:securityInfo");
        eaiHeader.appendChild(securityInfo);
        
        Element authentication = xmlDoc.createElementNS("urn:esbbank.com/gbo/xml/schemas/v1_0/", "NS1:authentication");
        securityInfo.appendChild(authentication);
        
        addHeaderElement(xmlDoc, authentication, "NS1:UserId", securityUserId);
        addHeaderElement(xmlDoc, authentication, "NS1:Password", securityPassword);
    }
    
    private void createEaiBody(Document xmlDoc, Element eaiMessage, Map<String, Object> requestData, String serviceName) {
        Element eaiBody = xmlDoc.createElementNS("urn:esbbank.com/gbo/xml/schemas/v1_0/", "NS1:eAI_BODY");
        eaiMessage.appendChild(eaiBody);
        
        Element eaiRequest = xmlDoc.createElementNS("urn:esbbank.com/gbo/xml/schemas/v1_0/", "NS1:eAI_REQUEST");
        eaiBody.appendChild(eaiRequest);
        
        String serviceRequestElementName = getServiceRequestElementName(serviceName);
        Element serviceRequest = xmlDoc.createElementNS("urn:esbbank.com/gbo/xml/schemas/v1_0/", "NS1:" + serviceRequestElementName);
        eaiRequest.appendChild(serviceRequest);
        
        populateXmlFromDataWithSequence(xmlDoc, serviceRequest, requestData, serviceName);
    }
    
    private void populateXmlFromData(Document doc, Element parent, Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (value instanceof Map) {
                Element childElement = doc.createElementNS("urn:esbbank.com/gbo/xml/schemas/v1_0/", "NS1:" + key);
                parent.appendChild(childElement);
                @SuppressWarnings("unchecked")
                Map<String, Object> mapValue = (Map<String, Object>) value;
                populateXmlFromData(doc, childElement, mapValue);
            } else {
                Element childElement = doc.createElementNS("urn:esbbank.com/gbo/xml/schemas/v1_0/", "NS1:" + key);
                childElement.setTextContent(value != null ? value.toString() : "");
                parent.appendChild(childElement);
            }
        }
    }
    
    private void populateXmlFromDataWithSequence(Document doc, Element parent, Map<String, Object> data, String serviceName) {
        try {
            // Get the sequence order from XSD
            org.w3c.dom.Document xsdDoc = xsdParserService.parseXsd(serviceName);
            String requestTypeName = getServiceRequestElementName(serviceName);
            List<String> sequenceOrder = xsdParserService.getElementSequenceFromXsd(xsdDoc, requestTypeName);
            
            if (!sequenceOrder.isEmpty()) {
                // Generate elements in XSD sequence order
                for (String elementName : sequenceOrder) {
                    if (data.containsKey(elementName)) {
                        Object value = data.get(elementName);
                        if (value instanceof Map) {
                            Element childElement = doc.createElementNS("urn:esbbank.com/gbo/xml/schemas/v1_0/", "NS1:" + elementName);
                            parent.appendChild(childElement);
                            @SuppressWarnings("unchecked")
                            Map<String, Object> mapValue = (Map<String, Object>) value;
                            populateXmlFromData(doc, childElement, mapValue);
                        } else {
                            Element childElement = doc.createElementNS("urn:esbbank.com/gbo/xml/schemas/v1_0/", "NS1:" + elementName);
                            childElement.setTextContent(value != null ? value.toString() : "");
                            parent.appendChild(childElement);
                        }
                    }
                }
                
                // Add any remaining elements not in the sequence (fallback)
                for (Map.Entry<String, Object> entry : data.entrySet()) {
                    String key = entry.getKey();
                    if (!sequenceOrder.contains(key)) {
                        Object value = entry.getValue();
                        if (value instanceof Map) {
                            Element childElement = doc.createElementNS("urn:esbbank.com/gbo/xml/schemas/v1_0/", "NS1:" + key);
                            parent.appendChild(childElement);
                            @SuppressWarnings("unchecked")
                            Map<String, Object> mapValue = (Map<String, Object>) value;
                            populateXmlFromData(doc, childElement, mapValue);
                        } else {
                            Element childElement = doc.createElementNS("urn:esbbank.com/gbo/xml/schemas/v1_0/", "NS1:" + key);
                            childElement.setTextContent(value != null ? value.toString() : "");
                            parent.appendChild(childElement);
                        }
                    }
                }
            } else {
                // Fallback to original behavior if sequence not found
                populateXmlFromData(doc, parent, data);
            }
        } catch (Exception e) {
            logger.warn("Could not parse XSD sequence for service {}, using fallback order", serviceName);
            // Fallback to original behavior
            populateXmlFromData(doc, parent, data);
        }
    }
    
    private void addHeaderElement(Document doc, Element parent, String elementName, Object value) {
        if (value != null) {
            Element element = doc.createElementNS("urn:esbbank.com/gbo/xml/schemas/v1_0/", elementName);
            element.setTextContent(value.toString());
            parent.appendChild(element);
        }
    }
    
    private String documentToString(Document doc) throws Exception {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.INDENT, "no");
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        transformer.transform(new DOMSource(doc), new StreamResult(outputStream));
        
        return outputStream.toString("UTF-8");
    }
    
    
    private String getServiceRequestElementName(String serviceName) {
        try {
            // Use existing XsdParserService to get the document
            org.w3c.dom.Document xsdDoc = xsdParserService.parseXsd(serviceName);
            return extractRequestElementNameFromDocument(xsdDoc);
        } catch (Exception e) {
            logger.warn("Could not parse XSD for service {}, using fallback naming", serviceName);
            return convertToCamelCase(serviceName) + "Request";
        }
    }
    
    private String extractRequestElementNameFromDocument(org.w3c.dom.Document xsdDoc) throws Exception {
        org.w3c.dom.NodeList elements = xsdDoc.getElementsByTagName("*");
        for (int i = 0; i < elements.getLength(); i++) {
            org.w3c.dom.Element element = (org.w3c.dom.Element) elements.item(i);
            String name = element.getAttribute("name");
            if (name != null && name.contains("Request") && 
                "complexType".equals(element.getNodeName())) {
                return name;
            }
        }
        throw new Exception("No Request complexType found in XSD");
    }
    
    private String convertToCamelCase(String serviceName) {
        String[] parts = serviceName.split("\\.");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].toLowerCase();
            if (i == 0) {
                result.append(part);
            } else {
                result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        
        return result.toString();
    }
}
