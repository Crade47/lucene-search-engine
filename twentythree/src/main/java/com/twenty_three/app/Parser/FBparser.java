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

public class FBparser {

    /**
     * Parses FBIS documents and extracts relevant fields.
     *
     * @param filePath Path to the FBIS document file.
     * @return A list of DocumentData objects containing the parsed fields.
     * @throws Exception if an error occurs while reading the file.
     */
    public List<DocumentData> parseFBIS(String filePath) throws Exception {
        List<DocumentData> parsedDocuments = new ArrayList<>();
        String filecontent = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");

        // Parse the content as XML
        Document parsedXml = Jsoup.parse(filecontent, "", org.jsoup.parser.Parser.xmlParser());

        // Process each <DOC> element
        for (Element docElement : parsedXml.select("DOC")) {
            try {
                // Extract document number
                String docNo = extractTagText(docElement, "DOCNO");

                // Extract and clean text content using a regular expression to remove tags
                String text = extractTextContent(docElement, "TEXT");

                // Extract date
                String date = extractTagText(docElement, "DATE1");

                // Construct a combined title from hierarchical tags
                String title = constructTitle(docElement);

                // Add the parsed document to the list
                parsedDocuments.add(new DocumentData(docNo, title, text, date));
            } catch (Exception e) {
                System.err.println("Error parsing document: " + e.getMessage());
            }
        }

        return parsedDocuments;
    }

    /**
     * Extracts plain text content from a specific tag in the document.
     *
     * @param parent The parent element to search within.
     * @param tagName The tag name to extract.
     * @return Text content of the tag, or null if not found.
     */
    private String extractTextContent(Element parent, String tagName) {
        Element tagElement = parent.selectFirst(tagName);
        if (tagElement == null) return null;

        // Remove tags using a regular expression
        return cleanTags(tagElement.text());
    }

    /**
     * Removes unwanted tags using a regular expression.
     *
     * @param content The original text content to clean.
     * @return Cleaned text without the specified tags.
     */
    private String cleanTags(String content) {
        // Predefined tags to remove
        String tagsToRemove = "(HT|HEADER|PHRASE|DATE1|ABS|FIG|TI|H1|H2|H3|H4|H5|H6|H7|H8|TR|TXT5|TEXT|AU)";
        
        // Regex to match the specified tags
        String regex = String.format("<\\/?(%s)>", tagsToRemove);

        // Replace tags with an empty string
        return content.replaceAll(regex, "").replaceAll("\\[|\\]", "").replaceAll("\n", " ").trim();
    }

    /**
     * Extracts text content from a specific tag or returns null if the tag is missing.
     *
     * @param parent The parent element to search within.
     * @param tagName The tag name to extract.
     * @return Text content of the tag, or null if not found.
     */
    private String extractTagText(Element parent, String tagName) {
        Element tagElement = parent.selectFirst(tagName);
        return tagElement != null ? tagElement.text() : null;
    }

    /**
     * Constructs a title by combining text from hierarchical tags (H3 to H8).
     *
     * @param docElement The parent document element.
     * @return The combined title string.
     */
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
