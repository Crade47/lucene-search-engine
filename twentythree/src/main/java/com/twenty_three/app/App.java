package com.twenty_three.app;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import com.twenty_three.app.Indexers.FBISIndex;
import com.twenty_three.app.Searcher.MultiDocSearcher;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class App {
    public static void main(String[] args) {
        String indexerType = "english"; // Default value
        String searcherType = "english"; // Default value
        String scoreType = "bm25"; // Default value

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--indexer") && i + 1 < args.length) {
                indexerType = args[i + 1].toLowerCase();
            } else if (args[i].equals("--searcher") && i + 1 < args.length) {
                searcherType = args[i + 1].toLowerCase();
            } else if (args[i].equals("--score") && i + 1 < args.length) {
                scoreType = args[i + 1].toLowerCase();
            }
        }

        Analyzer indexerAnalyzer = createAnalyzer(indexerType);
        Analyzer searcherAnalyzer = createAnalyzer(searcherType);
        Similarity similarity = createSimilarity(scoreType);

        System.out.println("Indexer Analyzer: " + indexerAnalyzer.getClass().getSimpleName());
        System.out.println("Searcher Analyzer: " + searcherAnalyzer.getClass().getSimpleName());
        System.out.println("Similarity: " + similarity.getClass().getSimpleName());
        // indexing and searching
        try {
            createIndex("corpus", indexerAnalyzer);
            search(searcherAnalyzer, similarity);
        } catch (Exception e) {
            System.err.println("Error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static Analyzer createAnalyzer(String type) {
        Map<String, Analyzer> analyzerMap = new HashMap<>();
        analyzerMap.put("standard", new StandardAnalyzer());
        analyzerMap.put("simple", new SimpleAnalyzer());
        analyzerMap.put("boolean", new StandardAnalyzer()); // todo:Assuming Boolean uses StandardAnalyzer
        analyzerMap.put("english", new EnglishAnalyzer());
        analyzerMap.put("whitespace", new WhitespaceAnalyzer());
        analyzerMap.put("custom", CustomAnalyzer.builder().build()); // todo:Assuming a default CustomAnalyzer
        analyzerMap.put("stopvector", new StandardAnalyzer()); // todo:Assuming Stopvector uses StandardAnalyzer
        return analyzerMap.getOrDefault(type, new EnglishAnalyzer());
    }

    private static void createIndex(String corpusDirectory, Analyzer analyzer) throws IOException {
        FBISIndex fbisIndex = new FBISIndex();
        fbisIndex.createFBISIndex(corpusDirectory, analyzer);
    }

    private static void search(Analyzer analyzer, Similarity similarity) throws Exception {
        MultiDocSearcher searcher = new MultiDocSearcher();
        searcher.search(analyzer, similarity);
    }

    private static Similarity createSimilarity(String type) {
        if ("vsm".equals(type)) {
            return new ClassicSimilarity();
        } else {
            return new BM25Similarity();
        }
    }
}