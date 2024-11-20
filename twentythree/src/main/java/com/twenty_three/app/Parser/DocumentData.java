package com.twenty_three.app.Parser;

public class DocumentData {
    private String docNo;
    private String title;
    private String text;
    private String date;
    private String summary; // New field for summary

    public DocumentData(String docNo, String title, String text, String date, String summary) {
        this.docNo = docNo;
        this.title = title;
        this.text = text;
        this.date = date;
        this.summary = summary;
    }

    public String getDocNo() {
        return docNo;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

    public String getDate() {
        return date;
    }

    public String getSummary() {
        return summary;
    }
}
