package com.twenty_three.app.parsers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;  

import com.twenty_three.app.Constants;
import com.twenty_three.app.Models.Ftobj;

public class FTparser {
    public static List<DocumentData> parseFT(String filePath) throws Exception {
        List<DocumentData> documents = new ArrayList<>();
        String content = new String(Files.readAllBytes(Paths.get(filePath)), "UTF-8");

        Document soup = Jsoup.parse(content, "", org.jsoup.parser.Parser.xmlParser());
        for (Element doc : soup.select("DOC")) {
            String docNo = doc.selectFirst("DOCNO").text();
            String title = doc.selectFirst("HEADLINE") != null ? doc.selectFirst("HEADLINE").text() : null;
            String text = doc.selectFirst("TEXT") != null ? doc.selectFirst("TEXT").text() : null;
            String date = doc.selectFirst("DATE") != null ? doc.selectFirst("DATE").text() : null;

            documents.add(new DocumentData(docNo, title, text, date, null));
        }
        return archive;
    }

    private static String getTag(Element element, String elem) {
        return element.selectFirst(elem) != null ? element.selectFirst(elem).text() : null;
    }
}



