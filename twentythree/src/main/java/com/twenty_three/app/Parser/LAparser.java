package com.twenty_three.app.parsers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.twenty_three.app.parsers.DocumentData;

public class LAparser {

    /**
     * Parses LA Times parsedDocuments from the specified file path.
     *
     * @param filePath The path to the document collection.
     * @return A list of DocumentData objects containing parsed fields.
     * @throws Exception If an error occurs during file processing.
     */
    public List<DocumentData> parseLATimes(String filePath) throws Exception {
        List<DocumentData> parsedDocuments = new ArrayList<>();
        String filecontent = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");

        // Parse the XML filecontent
        Document soup = Jsoup.parse(filecontent, "", org.jsoup.parser.Parser.xmlParser());

        // Loop through each <DOC> element
        for (Element doc : soup.select("DOC")) {
            String docNo = getTextOrNull(doc, "DOCNO");

            // Extract <HEADLINE> text from nested tags
            String title = null;
            Element headlineElement = doc.selectFirst("HEADLINE");
            if (headlineElement != null) {
                title = extractNestedText(headlineElement);
            }

            // Extract <TEXT> filecontent from nested tags
            String text = null;
            Element textElement = doc.selectFirst("TEXT");
            if (textElement != null) {
                text = extractNestedText(textElement);
            }

            // Extract <DATE> filecontent from nested tags
            String date = null;
            Element dateElement = doc.selectFirst("DATE");
            if (dateElement != null) {
                date = extractNestedText(dateElement);
            }

            // Extract <FIG> and <TXT5>
            String fig = doc.selectFirst("FIG") != null ? doc.selectFirst("FIG").text() : null;
            String txt5 = doc.selectFirst("TXT5") != null ? doc.selectFirst("TXT5").text() : null;

            // Add the parsed data to the list
            parsedDocuments.add(new DocumentData(docNo, title, text, date, null, fig, txt5));
        }

        return parsedDocuments;
    }

    /**
     * Retrieves the text filecontent of a given tag or returns null if the tag is missing.
     *
     * @param parent The parent element to search within
     * @param tagName The tag name to search for
     * @return Text filecontent of the tag, or null if not found
     */
    private String getTextOrNull(Element parent, String tagName) {
        Element element = parent.selectFirst(tagName);
        return element != null ? element.text() : null;
    }

    /**
     * Extracts text from nested elements recursively.
     *
     * @param element The parent element containing nested tags.
     * @return Combined text filecontent of all nested elements.
     */
    private String extractNestedText(Element element) {
        if (element == null) {
            return null;
        }
        StringBuilder textBuilder = new StringBuilder();

        // Traverse each child node
        for (Element child : element.children()) {
            textBuilder.append(child.text()).append(" "); // Add text filecontent of each child
        }

        return textBuilder.toString().trim(); // Return the concatenated text
    }
}