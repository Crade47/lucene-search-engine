package com.twenty_three.app.Parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;  
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;  

import com.twenty_three.app.Constants;
import com.twenty_three.app.Models.Ftobj;

public class FTparser {

    public static ArrayList<Ftobj> getDocuments(String filePath) throws IOException {
      //list that stores parsed data
        ArrayList<Ftobj> archive = new ArrayList<>();

        Document document = Jsoup.parse(new File(filePath), "UTF-8", "", org.jsoup.parser.Parser.xmlParser());
        Elements elements_t = document.select(Constants.docn);
        //extract each data tag and text
        for (Element element_t : elements_t) {
            Ftobj archinfo = new Ftobj(
                getTag(element_t, Constants.docnon),
                getTag(element_t, Constants.daten),
                getTag(element_t, Constants.headlinen),
                getTag(element_t, Constants.textn),
                getTag(element_t, Constants.pubn),
                getTag(element_t, Constants.profilen),
                getTag(element_t, Constants.bylinen)
            );
            archive.add(archinfo);
        }
        return archive;
    }

    private static String getTag(Element element, String elem) {
        return element.selectFirst(elem) != null ? element.selectFirst(elem).text() : null;
    }
    //recursive traverses files
    public static ArrayList<File> traverseFile(String dirdocument) {
        ArrayList<File> parsedata = new ArrayList<>();
        File dir = new File(dirdocument);

        if (dir.exists() && dir.isDirectory()) {
            File[] listf = dir.listFiles();
            for (File archf : listf) {
                if (archf.isDirectory()) {
                    parsedata.addAll(traverseFile(archf.getAbsolutePath()));
                } else if (!archf.getName().equals("readfrcg") && !archf.getName().equals("readmeft")) {
                    parsedata.add(archf);
                }
            }
        }
        return parsedata;
    }

    public static void main(String[] args) throws IOException {
        ArrayList<File> listf = traverseFile(Constants.DOCUMENT_PATH);
        if (listf.isEmpty()) {
            return;
        }
        for (File archf : listf) {
            System.out.println("Processing file: " + archf.getName());
            ArrayList<Ftobj> archive = getDocuments(archf.getAbsolutePath());

        }
    }
}



