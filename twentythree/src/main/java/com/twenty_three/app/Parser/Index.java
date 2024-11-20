package com.twenty_three.app.Parser;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


@FunctionalInterface
interface ParserFunction {
    List<DocumentData> parse(String filePath) throws Exception;
}

public class Index {

    public static void index() {
        try {
            // Define the directory for storing the index
            Path indexPath = Paths.get("index");
            Directory directory = FSDirectory.open(indexPath);

            // Set up the Lucene analyzer and configuration
            EnglishAnalyzer analyzer = new EnglishAnalyzer();
            IndexWriterConfig config = new IndexWriterConfig(analyzer);

            // Create the IndexWriter
            try (IndexWriter writer = new IndexWriter(directory, config)) {
                // Parse and combine all datasets
                List<DocumentData> allDocuments = parseAllCollections();

                // Index all parsed documents
                for (DocumentData docData : allDocuments) {
                    Document luceneDoc = createLuceneDocument(docData);
                    writer.addDocument(luceneDoc);
                }

                System.out.println("Indexing completed successfully!");
            }
        } catch (IOException e) {
            System.err.println("IOException occurred: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("An unexpected exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Parses all collections and combines the results into a single list.
     *
     * @return List of parsed DocumentData from all datasets
     * @throws Exception if parsing fails
     */
    private static List<DocumentData> parseAllCollections() throws Exception {
        List<DocumentData> allDocuments = new ArrayList<>();

        // Parse LA Times: Files directly under "latimes"
        System.out.println("Parsing LA Times documents...");
        String laTimesPath = "/home/azureuser/lucene-search-engine/twentythree/corpus/latimes";
        allDocuments.addAll(parseFilesInDirectory(laTimesPath, LAparser::parseLATimes));
        System.out.println("LA Times indexing complete!");

        //Parse Financial Times: Nested folders with files in "ft"
        System.out.println("Parsing Financial Times documents...");
        String ftPath = "/home/azureuser/lucene-search-engine/twentythree/corpus/ft";
        allDocuments.addAll(parseNestedFilesInDirectory(ftPath, FTparser::parseFT));
        System.out.println("Financial Times indexing complete!");

        // Parse FR94: Nested folders with files in "fr94"
        System.out.println("Parsing FR94 documents...");
        String fr94Path = "/home/azureuser/lucene-search-engine/twentythree/corpus/fr94";
        allDocuments.addAll(parseNestedFilesInDirectory(fr94Path, FR94parser::parseFR94));
        System.out.println("FR94 indexing complete!");

        // Parse FBIS: Files directly under "fbis"
        System.out.println("Parsing FBIS documents...");
        String fbisPath = "/home/azureuser/lucene-search-engine/twentythree/corpus/fbis";
        allDocuments.addAll(parseFilesInDirectory(fbisPath, FBparser::parseFBIS));
        System.out.println("FBIS indexing complete!");

        return allDocuments;
    }

    /**
     * Converts a DocumentData object into a Lucene Document.
     *
     * @param docData Parsed document data
     * @return Lucene Document
     */
    private static Document createLuceneDocument(DocumentData docData) {
        Document document = new Document();

        // Add DOCNO as a keyword field (not tokenized)
        if (docData.getDocNo() != null) {
            document.add(new StringField("docno", docData.getDocNo(), Field.Store.YES));
        }

        // Add title as a text field (tokenized for searching)
        if (docData.getTitle() != null) {
            document.add(new TextField("title", docData.getTitle(), Field.Store.YES));
        }

        // Add text content as a large text field (tokenized for searching)
        if (docData.getText() != null) {
            document.add(new TextField("content", docData.getText(), Field.Store.YES));
        }

        // Add date as a string field (stored but not tokenized)
        if (docData.getDate() != null) {
            document.add(new StringField("date", docData.getDate(), Field.Store.YES));
        }

        // Add summary as a text field (tokenized for searching) only if available
        if (docData.getSummary() != null) {
            document.add(new TextField("summary", docData.getSummary(), Field.Store.YES));
        }

        return document;
    }

    /**
     * Processes files directly under a single directory.
     *
     * @param directoryPath Path to the directory
     * @param parserFunction Function to parse files
     * @return List of DocumentData objects
     * @throws Exception if parsing fails
     */
    private static List<DocumentData> parseFilesInDirectory(String directoryPath, ParserFunction parserFunction) throws Exception {
        List<DocumentData> documents = new ArrayList<>();
        File dir = new File(directoryPath);

        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    documents.addAll(parserFunction.parse(file.getAbsolutePath()));
                }
            }
        }
        return documents;
    }

    /**
     * Processes files within nested directories.
     *
     * @param rootPath Path to the root directory
     * @param parserFunction Function to parse files
     * @return List of DocumentData objects
     * @throws Exception if parsing fails
     */
    private static List<DocumentData> parseNestedFilesInDirectory(String rootPath, ParserFunction parserFunction) throws Exception {
        List<DocumentData> documents = new ArrayList<>();
        File rootDir = new File(rootPath);

        if (rootDir.isDirectory()) {
            for (File subDir : rootDir.listFiles()) {
                if (subDir.isDirectory()) {
                    for (File file : subDir.listFiles()) {
                        if (file.isFile()) {
                            documents.addAll(parserFunction.parse(file.getAbsolutePath()));
                        }
                    }
                }
            }
        }
        return documents;
    }
}
