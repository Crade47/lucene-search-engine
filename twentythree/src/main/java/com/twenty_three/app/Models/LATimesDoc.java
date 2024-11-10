package com.twenty_three.app.Models;
public class LATimesDoc extends Base {
    private String docid, section, length, headline, byline, graphic, type, subject, correctionDate, correction;

    // Getters and setters for LATimesModel-specific fields
    public String getDocid() { return this.docid; }

    public void setDocid(String docid) { this.docid = docid; }

    public String getSection() { return this.section; }

    public void setSection(String section) { this.section = section; }

    public String getLength() { return this.length; }

    public void setLength(String length) { this.length = length; }

    public String getHeadline() { return this.headline; }

    public void setHeadline(String headline) { this.headline = headline; }

    public String getByline() { return this.byline; }

    public void setByline(String byline) { this.byline = byline; }

    public String getGraphic() { return this.graphic; }

    public void setGraphic(String graphic) { this.graphic = graphic; }

    public String getType() { return this.type; }

    public void setType(String type) { this.type = type; }

    public String getCorrectionDate() { return this.correctionDate; }

    public void setCorrectionDate(String correctionDate) { this.correctionDate = correctionDate; }

    public String getSubject() { return this.subject; }

    public void setSubject(String subject) { this.subject = subject; }

    public String getCorrection() { return this.correction; }

    public void setCorrection(String correction) { this.correction = correction; }
}
