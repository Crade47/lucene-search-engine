package com.twenty_three.app.Models;

public class FBISDoc extends Base {
    private String docid, region, country, eventSource, headline, language, originalId, additionalInfo;

    // Getters and setters for FBIS-specific fields
    public String getDocid() { return this.docid; }

    public void setDocid(String docid) { this.docid = docid; }

    public String getRegion() { return this.region; }

    public void setRegion(String region) { this.region = region; }

    public String getCountry() { return this.country; }

    public void setCountry(String country) { this.country = country; }

    public String getEventSource() { return this.eventSource; }

    public void setEventSource(String eventSource) { this.eventSource = eventSource; }

    public String getHeadline() { return this.headline; }

    public void setHeadline(String headline) { this.headline = headline; }

    public String getLanguage() { return this.language; }

    public void setLanguage(String language) { this.language = language; }

    public String getOriginalId() { return this.originalId; }

    public void setOriginalId(String originalId) { this.originalId = originalId; }

    public String getAdditionalInfo() { return this.additionalInfo; }

    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }
}
