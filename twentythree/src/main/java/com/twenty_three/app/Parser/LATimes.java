package com.twenty_three.app.Parser;

import com.twenty_three.app.Models.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LATimes {
    public List<LATimesDoc> parse(String filePath) {
        List<LATimesDoc> documents = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            StringBuilder docContent = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Start of a new document
                if (line.equals("<DOC>")) {
                    docContent = new StringBuilder();  // Reset content for a new document
                    docContent.append(line).append("\n");
                } 
                // End of the document
                else if (line.equals("</DOC>")) {
                    docContent.append(line);  // Append the closing tag
                    LATimesDoc doc = parseDocument(docContent.toString());
                    if (doc != null) {
                        documents.add(doc);  // Add the completed document
                    }
                } 
                // Append lines within a document
                else {
                    docContent.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return documents;
    }

    // Parse a single document using Jsoup
    private LATimesDoc parseDocument(String docContent) {
        LATimesDoc doc = new LATimesDoc();
        Document jsoupDoc = Jsoup.parse(docContent, "", org.jsoup.parser.Parser.xmlParser());

        // Extracting fields from the Jsoup document
        doc.setDocno(getElementText(jsoupDoc, "DOCNO"));
        doc.setDocid(getElementText(jsoupDoc, "DOCID"));
        doc.setDate(getElementText(jsoupDoc, "DATE"));
        doc.setSection(getElementText(jsoupDoc, "SECTION"));
        doc.setLength(getElementText(jsoupDoc, "LENGTH"));
        doc.setHeadline(getElementText(jsoupDoc, "HEADLINE"));
        doc.setByline(getElementText(jsoupDoc, "BYLINE"));
        doc.setText(getElementText(jsoupDoc, "Text"));
        doc.setCorrectionDate(getElementText(jsoupDoc, "CORRECTION-DATE"));
        doc.setCorrection(getElementText(jsoupDoc, "CORRECTION"));

        return doc;
    }

    // Helper method to get text content of an element by tag name
    private String getElementText(Document doc, String tag) {
        Element element = doc.selectFirst(tag);
        return element != null ? element.text() : "";
    }
}
