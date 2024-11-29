package com.twenty_three.app.parsers;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.search.similarities.Similarity;
import com.twenty_three.app.parsers.DocumentData;
import com.twenty_three.app.parsers.FTparser;
import com.twenty_three.app.parsers.FR94parser;
import com.twenty_three.app.parsers.LAparser;
import com.twenty_three.app.parsers.FBparser;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
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

    public static void main(String[] args) {
        try {
            // Define the directory for storing the index
            Path indexPath = Paths.get("index");
            Directory directory = FSDirectory.open(indexPath);

            // Set up the Lucene analyzer and similarity
            EnglishAnalyzer analyzer = new EnglishAnalyzer();
            BM25Similarity similarity = new BM25Similarity();

            // Create the IndexWriter
            IndexWriterConfig config = configureIndexWriter(analyzer, similarity);
            try (IndexWriter writer = new IndexWriter(directory, config)) {
                // Parse and combine all datasets
                List<DocumentData> parsedDocuments = parseAllCollections();

                // Index all parsed documents
                indexDocuments(writer, parsedDocuments);

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
     * Configures the IndexWriter with the specified analyzer and similarity.
     *
     * @param analyzer Analyzer for processing text
     * @param similarity Similarity metric for scoring
     * @return Configured IndexWriterConfig
     */
    private static IndexWriterConfig configureIndexWriter(Analyzer analyzer, Similarity similarity) {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        config.setSimilarity(similarity);
        return config;
    }

    /**
     * Parses all collections and combines the results into a single list.
     *
     * @return List of parsed DocumentData from all datasets
     * @throws Exception if parsing fails
     */
    private static List<DocumentData> parseAllCollections() throws Exception {
        List<DocumentData> parsedDocuments = new ArrayList<>();

        // Instantiate parser objects
        FBparser fbParser = new FBparser();
        FTparser ftParser = new FTparser();
        FR94parser fr94Parser = new FR94parser();
        LAparser laParser = new LAparser();

        // Parse LA Times: Files directly under "latimes"
        System.out.println("Parsing LA Times documents...");
        String laTimesPath = "/home/azureuser/lucene-search-engine/twentythree/corpus/latimes";
        parsedDocuments.addAll(parseFilesInDirectory(laTimesPath, laParser::parseLATimes));
        System.out.println("LA Times indexing complete!");

        // Parse Financial Times: Nested folders with files in "ft"
        System.out.println("Parsing Financial Times documents...");
        String ftPath = "/home/azureuser/lucene-search-engine/twentythree/corpus/ft";
        parsedDocuments.addAll(parseNestedFilesInDirectory(ftPath, ftParser::parseFT));
        System.out.println("Financial Times indexing complete!");

        // Parse FR94: Nested folders with files in "fr94"
        System.out.println("Parsing FR94 documents...");
        String fr94Path = "/home/azureuser/lucene-search-engine/twentythree/corpus/fr94";
        parsedDocuments.addAll(parseNestedFilesInDirectory(fr94Path, fr94Parser::parseFR94));
        System.out.println("FR94 indexing complete!");

        // Parse FBIS: Files directly under "fbis"
        System.out.println("Parsing FBIS documents...");
        String fbisPath = "/home/azureuser/lucene-search-engine/twentythree/corpus/fbis";
        parsedDocuments.addAll(parseFilesInDirectory(fbisPath, fbParser::parseFBIS));
        System.out.println("FBIS indexing complete!");

        return parsedDocuments;
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

        // Add author if available (specific to FT documents)
        if (docData.getAuthor() != null) {
            document.add(new TextField("author", docData.getAuthor(), Field.Store.YES));
        }
        
        return document;
    }

    /**
     * Indexes the parsed documents.
     *
     * @param writer IndexWriter instance
     * @param parsedDocuments List of parsed DocumentData
     */
    private static void indexDocuments(IndexWriter writer, List<DocumentData> parsedDocuments) {
        parsedDocuments.forEach(docData -> {
            try {
                Document luceneDoc = createLuceneDocument(docData);
                writer.addDocument(luceneDoc);
            } catch (IOException e) {
                System.err.println("Failed to add document to index: " + e.getMessage());
            }
        });
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
        List<DocumentData> parsedDocuments = new ArrayList<>();
        File dir = new File(directoryPath);

        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                if (file.isFile()) {
                    parsedDocuments.addAll(parserFunction.parse(file.getAbsolutePath()));
                }
            }
        }
        return parsedDocuments;
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
        List<DocumentData> parsedDocuments = new ArrayList<>();
        File rootDir = new File(rootPath);

        if (rootDir.isDirectory()) {
            for (File subDir : rootDir.listFiles()) {
                if (subDir.isDirectory()) {
                    for (File file : subDir.listFiles()) {
                        if (file.isFile()) {
                            parsedDocuments.addAll(parserFunction.parse(file.getAbsolutePath()));
                        }
                    }
                }
            }
        }
        return parsedDocuments;
    }
}