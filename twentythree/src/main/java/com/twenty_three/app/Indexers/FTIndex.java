package com.twenty_three.app.Indexers;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import com.twenty_three.app.Constants;
import com.twenty_three.app.Models.Ftobj;
import com.twenty_three.app.Parser.FTparser;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class FTIndex {

    public static void docuind(String indexDir, ArrayList<Ftobj> documents) throws IOException {
        Directory idir = FSDirectory.open(Paths.get(indexDir));
        EnglishAnalyzer analyzer = new EnglishAnalyzer();
        IndexWriterConfig inconfig = new IndexWriterConfig(analyzer);

        try (IndexWriter writer = new IndexWriter(idir, inconfig)) {
            for (Ftobj doc : documents) {
                Document document = new Document();

                document.add(new StringField("docno", doc.getDocNo() != null ? doc.getDocNo() : "", Field.Store.YES));
                document.add(new TextField("date", doc.getDate() != null ? doc.getDate() : "", Field.Store.YES));
                document.add(new TextField("headline", doc.getHeadline() != null ? doc.getHeadline() : "", Field.Store.YES));
                document.add(new TextField("text", doc.getText() != null ? doc.getText() : "", Field.Store.YES));
                document.add(new TextField("pub", doc.getPub() != null ? doc.getPub() : "", Field.Store.YES));
                document.add(new TextField("profile", doc.getProfile() != null ? doc.getProfile() : "", Field.Store.YES));
                document.add(new TextField("byline", doc.getByline() != null ? doc.getByline() : "", Field.Store.YES));

                writer.addDocument(document);
            }
        }
    }

    public static void main(String[] args) {
        try {
            String indexDir = "path to where to store index";
            ArrayList<File> files = FTparser.traverseFile(Constants.documentpath);
            ArrayList<Ftobj> allDocuments = new ArrayList<>();

            for (File file : files) {
                ArrayList<Ftobj> documents = FTparser.getDocuments(file.getAbsolutePath());
                allDocuments.addAll(documents);
            }

            docuind(indexDir, allDocuments);
            System.out.println("Indexing complete.");

        } catch (IOException e) {
            System.err.println("An error occurred during indexing:");
            e.printStackTrace();
        }
    }
}
