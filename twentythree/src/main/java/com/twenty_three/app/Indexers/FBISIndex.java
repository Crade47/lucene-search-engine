package com.twenty_three.app.Indexers;

import com.twenty_three.app.Constants;
import com.twenty_three.app.Models.FBISDoc;
import com.twenty_three.app.Parser.FBIS;
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

public class FBISIndex {

    // Directory where the search index will be saved
    private static final String INDEX_DIRECTORY = Constants.BASE_DIR + "/index/FBIS";

    public void createFBISIndex(String corpusDirectory) throws IOException {
        // Set up analyzers for each field
        Analyzer defaultAnalyzer = new EnglishAnalyzer();
        Map<String, Analyzer> analyzerMap = createAnalyzerMap();

        PerFieldAnalyzerWrapper analyzerWrapper = new PerFieldAnalyzerWrapper(defaultAnalyzer, analyzerMap);

        // Open the directory where the index will be stored
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

        // Set up an IndexWriterConfig with the PerFieldAnalyzerWrapper
        IndexWriterConfig config = new IndexWriterConfig(analyzerWrapper);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        // Create the IndexWriter to add documents to the index
        try (IndexWriter indexWriter = new IndexWriter(directory, config)) {
            // Initialize the FBIS parser
            FBIS parser = new FBIS();

            // Iterate over each file in the corpus directory
            Files.list(Paths.get(corpusDirectory))
                    .filter(Files::isRegularFile) // Process only regular files
                    .forEach(filePath -> {
                        try {
                            // Parse each file to get a list of FBISDoc
                            List<FBISDoc> parsedDocuments = parser.parse(filePath.toString());

                            // Convert each FBISDoc to a Lucene Document and add it to the index
                            for (FBISDoc fbisDoc : parsedDocuments) {
                                Document doc = createDocument(fbisDoc);
                                // Add the document to the index
                                indexWriter.addDocument(doc);
                            }

                        } catch (IOException e) {
                            System.err.println("Error parsing file: " + filePath);
                            e.printStackTrace();
                        }
                    });

            System.out.println("Indexing completed successfully FBIS!");

        } finally {
            directory.close();
        }
    }

    private Map<String, Analyzer> createAnalyzerMap() {
        Map<String, Analyzer> analyzerMap = new HashMap<>();
        analyzerMap.put("docno", new EnglishAnalyzer());
        analyzerMap.put("docid", new EnglishAnalyzer());
        analyzerMap.put("date", new EnglishAnalyzer());
        analyzerMap.put("region", new EnglishAnalyzer());
        analyzerMap.put("country", new EnglishAnalyzer());
        analyzerMap.put("eventSource", new EnglishAnalyzer());
        analyzerMap.put("title", new EnglishAnalyzer());
        analyzerMap.put("language", new EnglishAnalyzer());
        analyzerMap.put("originalId", new EnglishAnalyzer());
        analyzerMap.put("text", new EnglishAnalyzer());
        analyzerMap.put("additionalInfo", new EnglishAnalyzer());
        return analyzerMap;
    }

    private Document createDocument(FBISDoc fbisDoc) {
        Document doc = new Document();
        doc.add(new StringField("docno", fbisDoc.getDocno(), Field.Store.YES));
        doc.add(new StringField("docid", fbisDoc.getDocid(), Field.Store.YES));
        doc.add(new StringField("date", fbisDoc.getDate(), Field.Store.YES));
        doc.add(new StringField("region", fbisDoc.getRegion(), Field.Store.YES));
        doc.add(new StringField("country", fbisDoc.getCountry(), Field.Store.YES));
        doc.add(new TextField("eventSource", fbisDoc.getEventSource(), Field.Store.YES));
        doc.add(new TextField("title", fbisDoc.getHeadline(), Field.Store.YES));
        doc.add(new StringField("language", fbisDoc.getLanguage(), Field.Store.YES));
        doc.add(new StringField("originalId", fbisDoc.getOriginalId(), Field.Store.YES));
        doc.add(new TextField("text", fbisDoc.getText(), Field.Store.YES));
        doc.add(new StringField("additionalInfo", fbisDoc.getAdditionalInfo(), Field.Store.YES));
        return doc;
    }
}
