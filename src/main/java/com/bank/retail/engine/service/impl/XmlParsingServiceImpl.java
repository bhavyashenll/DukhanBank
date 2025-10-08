package com.bank.retail.engine.service.impl;

import com.bank.retail.engine.service.XmlParsingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class XmlParsingServiceImpl implements XmlParsingService {
    
    private static final Logger logger = LoggerFactory.getLogger(XmlParsingService.class);
    
    public List<Map<String, Object>> parseXmlResponse(String xmlResponse) throws Exception {
        logger.debug("Parsing XML response");
        
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        DocumentBuilder builder = factory.newDocumentBuilder();
        
        Document doc = builder.parse(new ByteArrayInputStream(xmlResponse.getBytes("UTF-8")));
        
        // Try to find eAI_REPLY elements with and without namespace
        NodeList eaiReplyNodes = doc.getElementsByTagName("eAI_REPLY");
        if (eaiReplyNodes.getLength() == 0) {
            // Try with namespace prefix
            eaiReplyNodes = doc.getElementsByTagName("NS1:eAI_REPLY");
        }
        if (eaiReplyNodes.getLength() == 0) {
            // Try with local name only
            eaiReplyNodes = doc.getElementsByTagNameNS("urn:esbbank.com/gbo/xml/schemas/v1_0/", "eAI_REPLY");
        }
        
        if (eaiReplyNodes.getLength() == 0) {
            logger.debug("No eAI_REPLY elements found");
            return new ArrayList<>();
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (int i = 0; i < eaiReplyNodes.getLength(); i++) {
            Element eaiReplyElement = (Element) eaiReplyNodes.item(i);
            
            // Parse all child elements of eAI_REPLY
            List<Map<String, Object>> replyData = parseGenericReplyData(eaiReplyElement);
            result.addAll(replyData);
        }
        
        logger.debug("Parsed {} data elements", result.size());
        return result;
    }
    
    private List<Map<String, Object>> parseGenericReplyData(Element eaiReplyElement) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // Get all direct child elements of eAI_REPLY
        NodeList childNodes = eaiReplyElement.getChildNodes();
        
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNode;
                String elementName = getLocalName(childElement.getNodeName());
                
                // Skip non-reply elements like returnCode, returnCodeDesc, etc.
                if (elementName.endsWith("Reply")) {
                    List<Map<String, Object>> elementData = parseReplyElement(childElement);
                    result.addAll(elementData);
                }
            }
        }
        
        return result;
    }
    
    private List<Map<String, Object>> parseReplyElement(Element replyElement) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        // Get all child elements of the reply
        NodeList childNodes = replyElement.getChildNodes();
        
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            
            if (childNode.getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNode;
                String elementName = getLocalName(childElement.getNodeName());
                
                // Check if this is a data element (not metadata like referenceNum, requestTime, etc.)
                if (isDataElement(elementName)) {
                    Map<String, Object> dataItem = new HashMap<>();
                    
                    // Extract all text content from child elements
                    NodeList dataNodes = childElement.getChildNodes();
                    for (int j = 0; j < dataNodes.getLength(); j++) {
                        Node dataNode = dataNodes.item(j);
                        if (dataNode.getNodeType() == Node.ELEMENT_NODE) {
                            Element dataElement = (Element) dataNode;
                            String fieldName = getLocalName(dataElement.getNodeName());
                            String fieldValue = dataElement.getTextContent();
                            
                            if (fieldValue != null && !fieldValue.trim().isEmpty()) {
                                dataItem.put(fieldName, fieldValue.trim());
                            }
                        }
                    }
                    
                    if (!dataItem.isEmpty()) {
                        result.add(dataItem);
                    }
                }
            }
        }
        
        return result;
    }
    
    private String getLocalName(String nodeName) {
        // Remove namespace prefix (e.g., "NS1:exchangeRate" -> "exchangeRate")
        int colonIndex = nodeName.indexOf(':');
        return colonIndex > 0 ? nodeName.substring(colonIndex + 1) : nodeName;
    }
    
    private boolean isDataElement(String elementName) {
        // Skip metadata elements, focus on actual data elements
        String[] skipElements = {"referenceNum", "referenceNumConsumer", "requestTime", "returnCode", "returnCodeDesc", "returnStatus"};
        for (String skip : skipElements) {
            if (elementName.equals(skip)) {
                return false;
            }
        }
        return true;
    }
    
}
