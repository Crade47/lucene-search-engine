package com.twenty_three.app.Indexers;

import com.twenty_three.app.Models.LATimesDoc;
import com.twenty_three.app.Parser.LATimes;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LATimesIndex {

    // Directory where the search index will be saved
    private static final String INDEX_DIRECTORY = "/home/azureuser/lucene-search-engine/index/LAT";

    public void createLAIndex(String corpusDirectory) throws IOException {
        // Set up analyzers for each field
        Analyzer defaultAnalyzer = new EnglishAnalyzer();
        Map<String, Analyzer> analyzerMap = new HashMap<>();
        analyzerMap.put("docno", new EnglishAnalyzer());
        analyzerMap.put("docid", new EnglishAnalyzer());
        analyzerMap.put("date", new EnglishAnalyzer());
        analyzerMap.put("section", new EnglishAnalyzer());
        analyzerMap.put("length", new EnglishAnalyzer());
        analyzerMap.put("headline", new EnglishAnalyzer());
        analyzerMap.put("byline", new EnglishAnalyzer());
        analyzerMap.put("text", new EnglishAnalyzer());

        PerFieldAnalyzerWrapper analyzerWrapper = new PerFieldAnalyzerWrapper(defaultAnalyzer, analyzerMap);

        // Open the directory where the index will be stored
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

        // Set up an IndexWriterConfig with the PerFieldAnalyzerWrapper
        IndexWriterConfig config = new IndexWriterConfig(analyzerWrapper);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        // Create the IndexWriter to add documents to the index
        try (IndexWriter indexWriter = new IndexWriter(directory, config)) {
            // Initialize the LATimes parser
            LATimes parser = new LATimes();

            // Iterate over each file in the corpus directory
            Files.list(Paths.get(corpusDirectory))
                .filter(Files::isRegularFile) // Process only regular files
                .forEach(filePath -> {
                    try {
                        // Parse each file to get a list of LATimesDoc
                        List<LATimesDoc> parsedDocuments = parser.parse(filePath.toString());

                        // Convert each LATimesDoc to a Lucene Document and add it to the index
                        for (LATimesDoc latimesDoc : parsedDocuments) {
                            Document doc = new Document();
                            doc.add(new StringField("docno", latimesDoc.getDocno(), Field.Store.YES));
                            doc.add(new StringField("docid", latimesDoc.getDocid(), Field.Store.YES));
                            doc.add(new StringField("date", latimesDoc.getDate(), Field.Store.YES));
                            doc.add(new StringField("section", latimesDoc.getSection(), Field.Store.YES));
                            doc.add(new StringField("length", latimesDoc.getLength(), Field.Store.YES));
                            doc.add(new TextField("title", latimesDoc.getHeadline(), Field.Store.YES));
                            doc.add(new TextField("byline", latimesDoc.getByline(), Field.Store.YES));
                            doc.add(new TextField("text", latimesDoc.getText(), Field.Store.YES));
                            doc.add(new StringField("correctionDate", latimesDoc.getCorrectionDate(), Field.Store.YES));
                            doc.add(new TextField("correction", latimesDoc.getCorrection(), Field.Store.YES));

                            // Add the document to the index
                            indexWriter.addDocument(doc);
                        }

                    } catch (IOException e) {
                        System.err.println("Error parsing file: " + filePath);
                        e.printStackTrace();
                    }
                });

            System.out.println("Indexing completed successfully! LATIMES");

        } finally {
            directory.close();
        }
    }
}
