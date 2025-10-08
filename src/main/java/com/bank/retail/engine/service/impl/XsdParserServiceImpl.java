package com.bank.retail.engine.service.impl;

import com.bank.retail.engine.service.XsdParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Service
public class XsdParserServiceImpl implements XsdParserService {
    
    private static final Logger logger = LoggerFactory.getLogger(XsdParserService.class);
    
    public Document parseXsd(String serviceName) throws Exception {
        logger.debug("Parsing XSD for service: {}", serviceName);
        
        try {
            String serviceFolderPath = "xsd/" + serviceName;
            String mainXsdPath = serviceFolderPath + "/" + serviceName + ".xsd";
            InputStream xsdStream = getClass().getClassLoader().getResourceAsStream(mainXsdPath);
            
            if (xsdStream == null) {
                String fallbackPath = "xsd/" + serviceName + ".xsd";
                xsdStream = getClass().getClassLoader().getResourceAsStream(fallbackPath);
                
                if (xsdStream == null) {
                    logger.error("XSD file not found for service: {}", serviceName);
                    throw new IllegalArgumentException("XSD file not found for service: " + serviceName + 
                        ". Expected location: " + mainXsdPath + " or " + fallbackPath);
                }
            }
            
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setValidating(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            
            return builder.parse(xsdStream);
        } catch (Exception e) {
            logger.error("Error parsing XSD for service: {}", serviceName, e);
            throw new Exception("Failed to parse XSD for service: " + serviceName, e);
        }
    }
    
    public List<String> getMandatoryFieldsFromXsd(Document xsdDoc) {
        logger.debug("Extracting mandatory fields from XSD");
        
        try {
            List<String> mandatoryFields = new ArrayList<>();
            
            NodeList elements = xsdDoc.getElementsByTagName("*");
            for (int i = 0; i < elements.getLength(); i++) {
                Element element = (Element) elements.item(i);
                
                if (isUnderRequestType(element) && 
                    element.hasAttribute("minOccurs") && 
                    "1".equals(element.getAttribute("minOccurs"))) {
                    
                    String elementName = element.getAttribute("name");
                    if (elementName != null && !elementName.isEmpty()) {
                        mandatoryFields.add(elementName);
                    }
                }
            }
            
            logger.debug("Found {} mandatory fields", mandatoryFields.size());
            return mandatoryFields;
        } catch (Exception e) {
            logger.error("Error extracting mandatory fields from XSD", e);
            throw new RuntimeException("Failed to extract mandatory fields from XSD", e);
        }
    }
    
    public List<String> getElementSequenceFromXsd(Document xsdDoc, String requestTypeName) {
        logger.debug("Extracting element sequence from XSD for request type: {}", requestTypeName);
        
        try {
            List<String> elementSequence = new ArrayList<>();
            
            // Find the complexType with the specified name
            NodeList complexTypes = xsdDoc.getElementsByTagName("complexType");
            for (int i = 0; i < complexTypes.getLength(); i++) {
                Element complexType = (Element) complexTypes.item(i);
                String typeName = complexType.getAttribute("name");
                
                if (requestTypeName.equals(typeName)) {
                    // Find the sequence element within this complexType
                    NodeList sequences = complexType.getElementsByTagName("sequence");
                    if (sequences.getLength() > 0) {
                        Element sequence = (Element) sequences.item(0);
                        NodeList sequenceElements = sequence.getChildNodes();
                        
                        for (int j = 0; j < sequenceElements.getLength(); j++) {
                            if (sequenceElements.item(j).getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                                Element seqElement = (Element) sequenceElements.item(j);
                                if ("element".equals(seqElement.getNodeName())) {
                                    String elementName = seqElement.getAttribute("name");
                                    if (elementName != null && !elementName.isEmpty()) {
                                        elementSequence.add(elementName);
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
            }
            
            logger.debug("Found {} elements in sequence for request type: {}", elementSequence.size(), requestTypeName);
            return elementSequence;
        } catch (Exception e) {
            logger.error("Error extracting element sequence from XSD for request type: {}", requestTypeName, e);
            throw new RuntimeException("Failed to extract element sequence from XSD", e);
        }
    }
    
    private boolean isUnderRequestType(Element element) {
        org.w3c.dom.Node parent = element.getParentNode();
        while (parent != null && parent.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
            if (parent instanceof Element) {
                Element parentElement = (Element) parent;
                String parentName = parentElement.getAttribute("name");
                if (parentName != null && parentName.contains("Request")) {
                    return true;
                }
                if (parentName != null && parentName.contains("Reply")) {
                    return false;
                }
            }
            parent = parent.getParentNode();
        }
        return false;
    }
}
