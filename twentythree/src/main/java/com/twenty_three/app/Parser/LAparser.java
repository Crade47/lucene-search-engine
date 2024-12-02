package com.twenty_three.app.Parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.twenty_three.app.Parser.ObjectData;

public class LAparser {

    public List<ObjectData> parseLATimes(String filePath) throws Exception {
        List<ObjectData> parsedDocuments = new ArrayList<>();
        String filecontent = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");

        // parse the XML filecontent
        Document soup = Jsoup.parse(filecontent, "", org.jsoup.parser.Parser.xmlParser());

        // looping through each <DOC> element
        for (Element doc : soup.select("DOC")) {
            String docNo = getTextOrNull(doc, "DOCNO");

            // extract <HEADLINE> text from nested tags
            String title = null;
            Element headlineElement = doc.selectFirst("HEADLINE");
            if (headlineElement != null) {
                title = extractNestedText(headlineElement);
            }

            // <TEXT> 
            String text = null;
            Element textElement = doc.selectFirst("TEXT");
            if (textElement != null) {
                text = extractNestedText(textElement);
            }

            //<DATE>
            String date = null;
            Element dateElement = doc.selectFirst("DATE");
            if (dateElement != null) {
                date = extractNestedText(dateElement);
            }


            // adding the parsed data to the list
            parsedDocuments.add(new ObjectData(docNo, title, text, date, null));
        }

        return parsedDocuments;
    }

    private String getTextOrNull(Element parent, String tagName) {
        Element element = parent.selectFirst(tagName);
        return element != null ? element.text() : null;
    }

    private String extractNestedText(Element element) {
        if (element == null) {
            return null;
        }
        StringBuilder textBuilder = new StringBuilder();

        // traverse every child node
        for (Element child : element.children()) {
            textBuilder.append(child.text()).append(" ");
        }

        return textBuilder.toString().trim(); 
    }
}