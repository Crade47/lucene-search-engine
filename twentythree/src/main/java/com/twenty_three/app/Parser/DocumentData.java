package com.twenty_three.app.parsers;

public class DocumentData {
    private String docNo;
    private String title;
    private String text;
    private String date;
    private String author; // Added for FTModel
    private String fig;    // Added for FbisModel
    private String txt5;   // Added for FbisModel

    public DocumentData(String docNo, String title, String text, String date) {
        this.docNo = docNo;
        this.title = title;
        this.text = text;
        this.date = date;
    }

    public DocumentData(String docNo, String title, String text, String date, String author, String fig, String txt5) {
        this.docNo = docNo;
        this.title = title;
        this.text = text;
        this.date = date;
        this.author = author;
        this.fig = fig;
        this.txt5 = txt5;
    }

    // Getters and setters for all fields
    public String getDocNo() { return docNo; }
    public String getTitle() { return title; }
    public String getText() { return text; }
    public String getDate() { return date; }
    public String getAuthor() { return author; }
    public String getFig() { return fig; }
    public String getTxt5() { return txt5; }

    public void setDocNo(String docNo) { this.docNo = docNo; }
    public void setTitle(String title) { this.title = title; }
    public void setText(String text) { this.text = text; }
    public void setDate(String date) { this.date = date; }
    public void setAuthor(String author) { this.author = author; }
    public void setFig(String fig) { this.fig = fig; }
    public void setTxt5(String txt5) { this.txt5 = txt5; }
}
