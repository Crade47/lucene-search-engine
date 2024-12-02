package com.twenty_three.app.Parser;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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
import org.apache.lucene.search.similarities.BM25Similarity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import com.twenty_three.app.Parser.MySynonymAnalyzer;

@FunctionalInterface
interface ParserFunction {
    List<ObjectData> parse(String filePath) throws Exception;
}

public class Index {

    public static void index(){
        try {
            // directory for storing the index
            Path indexPath = Paths.get("index");
            Directory directory = FSDirectory.open(indexPath);

            EnglishAnalyzer analyzer = new EnglishAnalyzer();
            //MySynonymAnalyzer analyzer = new MySynonymAnalyzer("/home/azureuser/lucene-search-engine/twentythree/wn_s.pl");
           // StandardAnalyzer analyzer = new StandardAnalyzer();

            BM25Similarity similarity = new BM25Similarity();
            //BooleanSimilarity similarity = new BooleanSimilarity();
            //LMJelinekMercerSimilarity similarity =new LMJelinekMercerSimilarity(0.7f);

            IndexWriterConfig config = configureIndexWriter(analyzer, similarity);
            try (IndexWriter writer = new IndexWriter(directory, config)) {
                // to parse and combine all datasets
                List<ObjectData> parsedDocuments = parseAllCollections();

                // index all parsed documents
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


    private static IndexWriterConfig configureIndexWriter(Analyzer analyzer, Similarity similarity) {
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND);
        config.setSimilarity(similarity);
        return config;
    }

    private static List<ObjectData> parseAllCollections() throws Exception {
        List<ObjectData> parsedDocuments = new ArrayList<>();

        // instantiate parser objects
        FBparser fbParser = new FBparser();
        FTparser ftParser = new FTparser();
        FR94parser fr94Parser = new FR94parser();
        LAparser laParser = new LAparser();

        // Parse LA Times
        System.out.println("Parsing LA Times documents...");
        String laTimesPath = "/home/azureuser/lucene-search-engine/twentythree/corpus/latimes";
        parsedDocuments.addAll(parseFilesInDirectory(laTimesPath, laParser::parseLATimes));
        System.out.println("LA Times indexing complete!");

        // Parse Financial Times
        System.out.println("Parsing Financial Times documents...");
        String ftPath = "/home/azureuser/lucene-search-engine/twentythree/corpus/ft";
        parsedDocuments.addAll(parseNestedFilesInDirectory(ftPath, ftParser::parseFT));
        System.out.println("Financial Times indexing complete!");

        // Parse FR94
        System.out.println("Parsing FR94 documents...");
        String fr94Path = "/home/azureuser/lucene-search-engine/twentythree/corpus/fr94";
        parsedDocuments.addAll(parseNestedFilesInDirectory(fr94Path, fr94Parser::parseFR94));
        System.out.println("FR94 indexing complete!");

        // Parse FBIS
        System.out.println("Parsing FBIS documents...");
        String fbisPath = "/home/azureuser/lucene-search-engine/twentythree/corpus/fbis";
        parsedDocuments.addAll(parseFilesInDirectory(fbisPath, fbParser::parseFBIS));
        System.out.println("FBIS indexing complete!");

        return parsedDocuments;
    }

    private static Document createLuceneDocument(ObjectData docData) {
        Document document = new Document();

        //DOCNO as a keyword field
        if (docData.getDocNo() != null) {
            document.add(new StringField("docno", docData.getDocNo(), Field.Store.YES));
        }

        // Add title as a text field 
        if (docData.getTitle() != null) {
            document.add(new TextField("title", docData.getTitle(), Field.Store.YES));
        }

        // Add text content as a large text field
        if (docData.getText() != null) {
            document.add(new TextField("content", docData.getText(), Field.Store.YES));
        }

        // Add date as a string field 
        if (docData.getDate() != null) {
            document.add(new StringField("date", docData.getDate(), Field.Store.YES));
        }

        // Add author if available
        if (docData.getAuthor() != null) {
            document.add(new TextField("author", docData.getAuthor(), Field.Store.YES));
        }
        
        return document;
    }

    private static void indexDocuments(IndexWriter writer, List<ObjectData> parsedDocuments) {
        parsedDocuments.forEach(docData -> {
            try {
                Document luceneDoc = createLuceneDocument(docData);
                writer.addDocument(luceneDoc);
            } catch (IOException e) {
                System.err.println("Failed to add document to index: " + e.getMessage());
            }
        });
    }

    private static List<ObjectData> parseFilesInDirectory(String directoryPath, ParserFunction parserFunction) throws Exception {
        List<ObjectData> parsedDocuments = new ArrayList<>();
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

    private static List<ObjectData> parseNestedFilesInDirectory(String rootPath, ParserFunction parserFunction) throws Exception {
        List<ObjectData> parsedDocuments = new ArrayList<>();
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