package com.twenty_three.app.parsers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import com.twenty_three.app.parsers.DocumentData;

public class LAparser {
    public static List<DocumentData> parseLATimes(String filePath) throws Exception {
        List<DocumentData> documents = new ArrayList<>();
        String content = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");

        // Parse the XML content
        Document soup = Jsoup.parse(content, "", org.jsoup.parser.Parser.xmlParser());

        // Loop through each <DOC> element
        for (Element doc : soup.select("DOC")) {
            String docNo = getTextOrNull(doc, "DOCNO");

            // Extract <HEADLINE> text from nested <P> tags
            String title = null;
            Element headlineElement = doc.selectFirst("HEADLINE");
            if (headlineElement != null) {
                title = extractNestedText(headlineElement);
            }

            // Extract <TEXT> content from nested <P> tags
            String text = null;
            Element textElement = doc.selectFirst("TEXT");
            if (textElement != null) {
                text = extractNestedText(textElement);
            }

            // Extract <DATE> content from nested <P> tags
            String date = null;
            Element dateElement = doc.selectFirst("DATE");
            if (dateElement != null) {
                date = extractNestedText(dateElement);
            }

            // Add the parsed data to the list
            documents.add(new DocumentData(docNo, title, text, date, null));
        }

        return documents;
    }

    /**
     * Retrieves the text content of a given tag or returns null if the tag is missing.
     *
     * @param parent The parent element to search within
     * @param tagName The tag name to search for
     * @return Text content of the tag, or null if not found
     */
    private static String getTextOrNull(Element parent, String tagName) {
        Element element = parent.selectFirst(tagName);
        return element != null ? element.text() : null;
    }

    /**
     * Extracts text from nested elements, ignoring formatting or spaces.
     *
     * @param element The parent element containing nested tags
     * @return Combined text content of all nested elements
     */
    private static String extractNestedText(Element element) {
        return element.text();
    }
}

