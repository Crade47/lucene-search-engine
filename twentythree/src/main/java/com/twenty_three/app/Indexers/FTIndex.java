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
    private static final String INDEX_DIRECTORY = Constants.BASE_DIR + "/index/FT";

    public void createFTIndex(String corpusDirectory) throws IOException {

        ArrayList<File> files = FTparser.traverseFile(corpusDirectory);
        ArrayList<Ftobj> allDocuments = new ArrayList<>();

        for (File file : files) {
            ArrayList<Ftobj> documents = FTparser.getDocuments(file.getAbsolutePath());
            allDocuments.addAll(documents);
        }

        Directory idir = FSDirectory.open(Paths.get(INDEX_DIRECTORY));
        EnglishAnalyzer analyzer = new EnglishAnalyzer();
        IndexWriterConfig inconfig = new IndexWriterConfig(analyzer);

        try (IndexWriter writer = new IndexWriter(idir, inconfig)) {
            for (Ftobj doc : allDocuments) {
                Document document = new Document();

                document.add(new StringField("docno", doc.getDocNo() != null ? doc.getDocNo() : "", Field.Store.YES));
                document.add(new TextField("date", doc.getDate() != null ? doc.getDate() : "", Field.Store.YES));
                document.add(
                        new TextField("title", doc.getHeadline() != null ? doc.getHeadline() : "", Field.Store.YES));
                document.add(new TextField("text", doc.getText() != null ? doc.getText() : "", Field.Store.YES));
                document.add(new TextField("pub", doc.getPub() != null ? doc.getPub() : "", Field.Store.YES));
                document.add(
                        new TextField("profile", doc.getProfile() != null ? doc.getProfile() : "", Field.Store.YES));
                document.add(new TextField("byline", doc.getByline() != null ? doc.getByline() : "", Field.Store.YES));

                writer.addDocument(document);

            }
        }
        System.out.println("Indexing completed successfully FT!");
    }

    // public static void main(String[] args) {
    // try {
    // ArrayList<File> files = FTparser.traverseFile(Constants.documentpath);
    // ArrayList<Ftobj> allDocuments = new ArrayList<>();

    // for (File file : files) {
    // ArrayList<Ftobj> documents = FTparser.getDocuments(file.getAbsolutePath());
    // allDocuments.addAll(documents);
    // }

    // docuind( allDocuments);
    // System.out.println("Indexing complete.");

    // } catch (IOException e) {
    // System.err.println("An error occurred during indexing:");
    // e.printStackTrace();
    // }
    // }
}
