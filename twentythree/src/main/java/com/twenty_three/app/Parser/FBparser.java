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

public class FBparser {


    public List<ObjectData> parseFBIS(String filePath) throws Exception {
        List<ObjectData> parsedDocuments = new ArrayList<>();
        String filecontent = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");

        // parse as XML
        Document parsedXml = Jsoup.parse(filecontent, "", org.jsoup.parser.Parser.xmlParser());

        // to process each <DOC> element
        for (Element docElement : parsedXml.select("DOC")) {
            try {
                // extract data
                String docNo = extractTagText(docElement, "DOCNO");

                String text = extractTextContent(docElement, "TEXT");

                String date = extractTagText(docElement, "DATE1");

                String title = constructTitle(docElement);

                // ddd the parsed document to the list
                parsedDocuments.add(new ObjectData(docNo, title, text, date));
            } catch (Exception e) {
                System.err.println("Error parsing document: " + e.getMessage());
            }
        }

        return parsedDocuments;
    }

    private String extractTextContent(Element parent, String tagName) {
        Element tagElement = parent.selectFirst(tagName);
        if (tagElement == null) return null;

        // remove tags using a regular expression
        return cleanTags(tagElement.text());
    }

    private String cleanTags(String content) {
        //remove predefined tags
        String tagsToRemove = "(HT|HEADER|PHRASE|DATE1|ABS|FIG|TI|H1|H2|H3|H4|H5|H6|H7|H8|TR|TXT5|TEXT|AU)";
        
        //  match the specified tags
        String regex = String.format("<\\/?(%s)>", tagsToRemove);

        // replace tags with empty string
        return content.replaceAll(regex, "").replaceAll("\\[|\\]", "").replaceAll("\n", " ").trim();
    }

    private String extractTagText(Element parent, String tagName) {
        Element tagElement = parent.selectFirst(tagName);
        return tagElement != null ? tagElement.text() : null;
    }

    private String constructTitle(Element docElement) {
        List<String> titleParts = new ArrayList<>();
        for (int level = 3; level <= 8; level++) {
            String tagName = "H" + level;
            Element tagElement = docElement.selectFirst(tagName);
            if (tagElement != null) {
                titleParts.add(tagElement.text());
            }
        }
        return String.join(" ", titleParts).trim();
    }
}
