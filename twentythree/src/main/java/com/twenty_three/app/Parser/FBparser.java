package com.twenty_three.app.Parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.twenty_three.app.Parser.DocumentData;

public class FBparser {

    // Predefined tags to process or clean
    private static final List<String> TAGS_TO_REMOVE = Arrays.asList(
        "TI", "HT", "PHRASE", "DATE1", "ABS", "FIG",
        "H1", "H2", "H3", "H4", "H5", "H6", "H7", "H8",
        "TR", "TXT5", "HEADER", "TEXT", "AU"
    );

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
                String documentNumber = extractTagText(docElement, "DOCNO");

                // Extract and clean text content
                String documentText = extractTagText(docElement, "TEXT");
                if (documentText != null) {
                    documentText = removeSpecificTags(documentText);
                }

                // Extract date
                String documentDate = extractTagText(docElement, "DATE1");

                // Construct a combined title from hierarchical tags
                StringBuilder titleBuilder = new StringBuilder();
                for (int level = 3; level <= 8; level++) {
                    String tagName = "H" + level;
                    Element tagElement = docElement.selectFirst(tagName);
                    if (tagElement != null) {
                        titleBuilder.append(tagElement.text()).append(" ");
                    }
                }
                String documentTitle = titleBuilder.toString().trim();

                // Add the parsed document to the list
                parsedDocuments.add(new DocumentData(documentNumber, documentTitle, documentText, documentDate));
            } catch (Exception e) {
                System.err.println("Error parsing document: " + e.getMessage());
            }
        }

        return parsedDocuments;
    }

    /**
     * Extracts text content from a specific tag in the document.
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
     * Cleans the text content by removing specific predefined tags.
     *
     * @param content The original text content to clean.
     * @return Cleaned text without the specified tags.
     */
    private String removeSpecificTags(String content) {
        // Replace newlines and square brackets
        content = content.replaceAll("\\[|\\]", "").replaceAll("\n", " ").trim();

        // Remove each tag in the predefined list
        for (String tag : TAGS_TO_REMOVE) {
            content = content.replaceAll("<" + tag + ">", "")
                             .replaceAll("</" + tag + ">", "");
        }

        return content;
    }
}