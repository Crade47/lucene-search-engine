package com.twenty_three.app.Parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.twenty_three.app.Parser.ObjectData;

public class FR94parser {

    public List<ObjectData> parseFR94(String filePath) throws Exception {
        List<ObjectData> parsedDocuments = new ArrayList<>();
        String filecontent = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");

        Document soup = Jsoup.parse(filecontent, "", org.jsoup.parser.Parser.xmlParser());
        for (Element doc : soup.select("DOC")) {
            String docNo = doc.selectFirst("DOCNO").text();
            String title = doc.selectFirst("DOCTITLE") != null ? doc.selectFirst("DOCTITLE").text() : null;
            String text = doc.selectFirst("TEXT") != null ? extractAndCleanText(doc.selectFirst("TEXT")) : null;
            String date = doc.selectFirst("DATE") != null ? doc.selectFirst("DATE").text() : null;

            parsedDocuments.add(new ObjectData(docNo, title, text, date));
        }
        return parsedDocuments;
    }

    private String extractAndCleanText(Element textElement) {
        if (textElement == null) {
            return null;
        }

        // removing comments within the <TEXT> element
        for (Node node : textElement.childNodes()) {
            if (node.nodeName().equals("#comment")) {
                node.remove(); 
            }
        }

        return textElement.text().trim();
    }
}