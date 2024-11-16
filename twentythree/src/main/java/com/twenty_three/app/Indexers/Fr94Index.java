package com.twenty_three.app.Indexers;

import com.twenty_three.app.Models.Fr94Doc;
import com.twenty_three.app.Parser.Fr94;
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

public class Fr94Index {

    // Directory where the search index will be saved
    private static final String INDEX_DIRECTORY = "/home/azureuser/lucene-search-engine/index/Fr94";

    public void createFRIndex(String corpusDirectory) throws IOException {
        // Set up analyzers for each field
        Analyzer defaultAnalyzer = new EnglishAnalyzer();
        Map<String, Analyzer> analyzerMap = new HashMap<>();
        analyzerMap.put("docno", new EnglishAnalyzer());
        analyzerMap.put("date", new EnglishAnalyzer());
        analyzerMap.put("fr", new EnglishAnalyzer());
        analyzerMap.put("text", new EnglishAnalyzer());
        analyzerMap.put("footcite", new EnglishAnalyzer());
        analyzerMap.put("cfrno", new EnglishAnalyzer());
        analyzerMap.put("rindock", new EnglishAnalyzer());
        analyzerMap.put("usDept", new EnglishAnalyzer());
        analyzerMap.put("usBureau", new EnglishAnalyzer());
        analyzerMap.put("imports", new EnglishAnalyzer());
        analyzerMap.put("doctitle", new EnglishAnalyzer());
        analyzerMap.put("agency", new EnglishAnalyzer());
        analyzerMap.put("action", new EnglishAnalyzer());
        analyzerMap.put("summary", new EnglishAnalyzer());
        analyzerMap.put("address", new EnglishAnalyzer());
        analyzerMap.put("further", new EnglishAnalyzer());
        analyzerMap.put("supplem", new EnglishAnalyzer());
        analyzerMap.put("signer", new EnglishAnalyzer());
        analyzerMap.put("signjob", new EnglishAnalyzer());
        analyzerMap.put("frFiling", new EnglishAnalyzer());
        analyzerMap.put("billing", new EnglishAnalyzer());
        analyzerMap.put("footnote", new EnglishAnalyzer());
        analyzerMap.put("footname", new EnglishAnalyzer());

        PerFieldAnalyzerWrapper analyzerWrapper = new PerFieldAnalyzerWrapper(defaultAnalyzer, analyzerMap);

        // Open the directory where the index will be stored
        Directory directory = FSDirectory.open(Paths.get(INDEX_DIRECTORY));

        // Set up an IndexWriterConfig with the PerFieldAnalyzerWrapper
        IndexWriterConfig config = new IndexWriterConfig(analyzerWrapper);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        // Create the IndexWriter to add documents to the index
        try (IndexWriter indexWriter = new IndexWriter(directory, config)) {
            // Initialize the Fr94 parser
            Fr94 parser = new Fr94();

            // Iterate over each file in the corpus directory
            Files.list(Paths.get(corpusDirectory))
                .filter(Files::isRegularFile) // Process only regular files
                .forEach(filePath -> {
                    try {
                        // Parse each file to get a list of Fr94Doc
                        List<Fr94Doc> parsedDocuments = parser.parse(filePath.toString());

                        // Convert each Fr94Doc to a Lucene Document and add it to the index
                        for (Fr94Doc fr94Doc : parsedDocuments) {
                            Document doc = new Document();
                            doc.add(new StringField("docno", fr94Doc.getDocno(), Field.Store.YES));
                            doc.add(new StringField("date", fr94Doc.getDate(), Field.Store.YES));
                            doc.add(new StringField("fr", fr94Doc.getFr(), Field.Store.YES));
                            doc.add(new TextField("text", fr94Doc.getText(), Field.Store.YES));
                            doc.add(new TextField("footcite", fr94Doc.getFootcite(), Field.Store.YES));
                            doc.add(new TextField("cfrno", fr94Doc.getCfrno(), Field.Store.YES));
                            doc.add(new TextField("rindock", fr94Doc.getRindock(), Field.Store.YES));
                            doc.add(new TextField("usDept", fr94Doc.getUsDept(), Field.Store.YES));
                            doc.add(new TextField("usBureau", fr94Doc.getUsBureau(), Field.Store.YES));
                            doc.add(new TextField("imports", fr94Doc.getImports(), Field.Store.YES));
                            doc.add(new TextField("title", fr94Doc.getDoctitle(), Field.Store.YES));
                            doc.add(new TextField("agency", fr94Doc.getAgency(), Field.Store.YES));
                            doc.add(new TextField("action", fr94Doc.getAction(), Field.Store.YES));
                            doc.add(new TextField("summary", fr94Doc.getSummary(), Field.Store.YES));
                            doc.add(new TextField("address", fr94Doc.getAddress(), Field.Store.YES));
                            doc.add(new TextField("further", fr94Doc.getFurther(), Field.Store.YES));
                            doc.add(new TextField("supplem", fr94Doc.getSupplem(), Field.Store.YES));
                            doc.add(new TextField("signer", fr94Doc.getSigner(), Field.Store.YES));
                            doc.add(new TextField("signjob", fr94Doc.getSignjob(), Field.Store.YES));
                            doc.add(new TextField("frFiling", fr94Doc.getFrFiling(), Field.Store.YES));
                            doc.add(new TextField("billing", fr94Doc.getBilling(), Field.Store.YES));
                            doc.add(new TextField("footnote", fr94Doc.getFootnote(), Field.Store.YES));
                            doc.add(new TextField("footname", fr94Doc.getFootname(), Field.Store.YES));

                            // Add the document to the index
                            indexWriter.addDocument(doc);
                        }

                    } catch (IOException e) {
                        System.err.println("Error parsing file: " + filePath);
                        e.printStackTrace();
                    }
                });

            System.out.println("Indexing completed successfully! FR94");

        } finally {
            directory.close();
        }
    }
}
