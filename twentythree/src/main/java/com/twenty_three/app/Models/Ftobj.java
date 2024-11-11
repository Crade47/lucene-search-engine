package com.twenty_three.app.Models;

//hold document data
public class Ftobj {
    private final String docno;
    private final String date;
    private final String headline;
    private final String text;
    private final String pub;
    private final String profile;  
    private final String byline; 

    public Ftobj(String docno, String date, String headline, String text, 
                      String pub, String profile, String byline) {
        this.docno = docno;
        this.date = date;
        this.headline = headline;
        this.text = text;
        this.pub = pub;
        this.profile = profile;
        this.byline = byline;
    }

    public String getDocNo() { return docno; }
    public String getDate() { return date; }
    public String getHeadline() { return headline; }
    public String getText() { return text; }
    public String getPub() { return pub; }
    public String getProfile() { return profile; }
    public String getByline() { return byline; }
}
