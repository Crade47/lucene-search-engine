package com.twenty_three.app.parsers;

public class DocumentData {
    private String docNo;
    private String title;
    private String text;
    private String date;
    private String author;

    public DocumentData(String docNo, String title, String text, String date) {
        this.docNo = docNo;
        this.title = title;
        this.text = text;
        this.date = date;
    }

    public DocumentData(String docNo, String title, String text, String date, String author) {
        this.docNo = docNo;
        this.title = title;
        this.text = text;
        this.date = date;
        this.author = author;
    }

    // Getters and setters for all fields
    public String getDocNo() { return docNo; }
    public String getTitle() { return title; }
    public String getText() { return text; }
    public String getDate() { return date; }
    public String getAuthor() { return author; }


    public void setDocNo(String docNo) { this.docNo = docNo; }
    public void setTitle(String title) { this.title = title; }
    public void setText(String text) { this.text = text; }
    public void setDate(String date) { this.date = date; }
    public void setAuthor(String author) { this.author = author; }
}
