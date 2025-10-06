package com.dukhan.MQ.Helpers.dto;

public class XmlResponse {
    
    private String xml;
    
    public XmlResponse() {}
    
    public XmlResponse(String xml) {
        this.xml = xml;
    }
    
    // Getters and Setters
    public String getXml() {
        return xml;
    }
    
    public void setXml(String xml) {
        this.xml = xml;
    }
}
