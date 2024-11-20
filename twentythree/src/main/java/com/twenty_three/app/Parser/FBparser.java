package com.twenty_three.app.Parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import com.twenty_three.app.Parser.DocumentData;

public class FBparser {
    public static List<DocumentData> parseFBIS(String filePath) throws Exception {
        List<DocumentData> documents = new ArrayList<>();
        String content = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");

        // Parse the file as XML
        Document soup = Jsoup.parse(content, "", org.jsoup.parser.Parser.xmlParser());

        // Loop through each <DOC> block
        for (Element doc : soup.select("DOC")) {
            try {
                // Extract <DOCNO>
                String docNo = getTextOrNull(doc, "DOCNO");

                // Extract <H3> and <TI> (title) from <HEADER> if available
                String title = null;
                Element headerElement = doc.selectFirst("HEADER");
                if (headerElement != null) {
                    Element h3Element = headerElement.selectFirst("H3");
                    if (h3Element != null) {
                        Element tiElement = h3Element.selectFirst("TI");
                        if (tiElement != null) {
                            title = tiElement.text();
                        }
                    }
                }

                // Extract <TEXT> content
                String text = getTextOrNull(doc, "TEXT");

                // Extract <DATE1> from <HEADER>
                String date = null;
                if (headerElement != null) {
                    Element dateElement = headerElement.selectFirst("DATE1");
                    if (dateElement != null) {
                        date = dateElement.text();
                    }
                }

                // Add the parsed document data to the list
                documents.add(new DocumentData(docNo, title, text, date, null));
            } catch (Exception e) {
                // Log any issues with parsing specific documents
                System.err.println("Error parsing document: " + e.getMessage());
            }
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
}