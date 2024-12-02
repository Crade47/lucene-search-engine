package com.twenty_three.app.Parser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import com.twenty_three.app.Parser.ObjectData;

public class FTparser {
    public List<ObjectData> parseFT(String filePath) throws Exception {
        List<ObjectData> parsedDocuments = new ArrayList<>();
        String filecontent = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");

        Document soup = Jsoup.parse(filecontent, "", org.jsoup.parser.Parser.xmlParser());
        for (Element doc : soup.select("DOC")) {
            String docNo = doc.selectFirst("DOCNO").text();
            String title = doc.selectFirst("HEADLINE") != null ? doc.selectFirst("HEADLINE").text() : null;
            String text = doc.selectFirst("TEXT") != null ? doc.selectFirst("TEXT").text() : null;
            String date = doc.selectFirst("DATE") != null ? doc.selectFirst("DATE").text() : null;
            String author = doc.selectFirst("BYLINE") != null ? doc.selectFirst("BYLINE").text() : null;

            parsedDocuments.add(new ObjectData(docNo, title, text, date, author));
        }
        return parsedDocuments;
    }
}



