package com.twenty_three.app.parsers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import com.twenty_three.app.parsers.DocumentData;

public class FR94parser {
    public static List<DocumentData> parseFR94(String filePath) throws Exception {
        List<DocumentData> documents = new ArrayList<>();
        String content = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");

        // Parse the file using Jsoup as XML
        Document soup = Jsoup.parse(content, "", org.jsoup.parser.Parser.xmlParser());

        // Loop through each <DOC> element
        for (Element doc : soup.select("DOC")) {
            try {
                // Extract <DOCNO>
                String docNo = doc.selectFirst("DOCNO").text();

                // Extract <DOCTITLE> (optional)
                String title = getTextOrNull(doc, "DOCTITLE");

                // Extract <DATE> (optional)
                String date = getTextOrNull(doc, "DATE");

                // Extract <SUMMARY> (optional)
                String summary = getTextOrNull(doc, "SUMMARY");

                // Extract and clean <TEXT>
                String text = extractAndCleanText(doc.selectFirst("TEXT"));

                // Add the parsed document data to the list
                documents.add(new DocumentData(docNo, title, text, date, summary));
            } catch (Exception e) {
                // Log any issues with parsing specific documents
                System.err.println("Error parsing document: " + e.getMessage());
            }
        }
        return documents;
    }

    /**
     * Extracts the content of an element, removing comments and cleaning unnecessary nodes.
     * 
     * @param textElement The <TEXT> element to process
     * @return Cleaned text content, or null if the element is null
     */
    private static String extractAndCleanText(Element textElement) {
        if (textElement == null) {
            return null;
        }
        // Remove all comments within the <TEXT> element
        for (Node node : textElement.childNodes()) {
            if (node.nodeName().equals("#comment")) {
                node.remove(); // Remove comments like <!-- PJG QTAG 02 -->
            }
        }
        // Return cleaned text
        return textElement.text().trim();
    }

    /**
     * Retrieves the text content of a given tag or returns null if the tag is missing.
     * 
     * @param doc The parent element to search within
     * @param tagName The tag name to search for
     * @return Text content of the tag, or null if not found
     */
    private static String getTextOrNull(Element doc, String tagName) {
        Element element = doc.selectFirst(tagName);
        return element != null ? element.text() : null;
    }
}
