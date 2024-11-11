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

public class Fr94 {
   
        public List<Fr94Doc> parse(String filePath) {
            List<Fr94Doc> documents = new ArrayList<>();
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
                        Fr94Doc doc = parseDocument(docContent.toString());
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
        private Fr94Doc parseDocument(String docContent) {
            Fr94Doc doc = new Fr94Doc();
            Document jsoupDoc = Jsoup.parse(docContent, "", org.jsoup.parser.Parser.xmlParser());
            
            // Extracting fields from the Jsoup document
            doc.setDocno(getElementText(jsoupDoc, "DOCNO"));
            doc.setDate(getElementText(jsoupDoc, "DATE"));
            doc.setFr(getElementText(jsoupDoc, "FR"));
            doc.setText(getElementText(jsoupDoc, "TEXT"));
            
            // Additional metadata fields
            doc.setFootcite(getElementText(jsoupDoc, "FOOTCITE"));
            doc.setCfrno(getElementText(jsoupDoc, "CFRNO"));
            doc.setRindock(getElementText(jsoupDoc, "RINDOCK"));
            doc.setUsDept(getElementText(jsoupDoc, "US-DEPT"));
            doc.setUsBureau(getElementText(jsoupDoc, "US-BUREAU"));
            doc.setImports(getElementText(jsoupDoc, "IMPORTS"));
            doc.setDoctitle(getElementText(jsoupDoc, "DOCTITLE"));
            doc.setAgency(getElementText(jsoupDoc, "AGENCY"));
            doc.setAction(getElementText(jsoupDoc, "ACTION"));
            doc.setSummary(getElementText(jsoupDoc, "SUMMARY"));
            doc.setAddress(getElementText(jsoupDoc, "ADDRESS"));
            doc.setFurther(getElementText(jsoupDoc, "FURTHER"));
            doc.setSupplem(getElementText(jsoupDoc, "SUPPLEM"));
            doc.setSigner(getElementText(jsoupDoc, "SIGNER"));
            doc.setSignjob(getElementText(jsoupDoc, "SIGNJOB"));
            doc.setFrFiling(getElementText(jsoupDoc, "FR-FILING"));
            doc.setBilling(getElementText(jsoupDoc, "BILLING"));
            doc.setFootnote(getElementText(jsoupDoc, "FOOTNOTE"));
            doc.setFootname(getElementText(jsoupDoc, "FOOTNAME"));
    
            return doc;
        }
    
        // Helper method to get text content of an element by tag name
        private String getElementText(Document doc, String tag) {
            Element element = doc.selectFirst(tag);
            return element != null ? element.text() : "";
        }
    }
    
