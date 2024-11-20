package com.twenty_three.app.Parser;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class Search {

    public static void search() {
        try {
            // Define the directory where the index is stored
            Path indexPath = Paths.get("/home/azureuser/lucene-search-engine/twentythree/index");
            IndexReader reader = DirectoryReader.open(FSDirectory.open(indexPath));
            IndexSearcher searcher = new IndexSearcher(reader);

            // Set BM25 as the similarity model
            searcher.setSimilarity(new BM25Similarity());

            // Use the EnglishAnalyzer for querying
            EnglishAnalyzer analyzer = new EnglishAnalyzer();

            // Use MultiFieldQueryParser for querying content and title
            MultiFieldQueryParser parser = new MultiFieldQueryParser(
                new String[]{"content", "title"}, // Fields to search
                analyzer
            );

            // Path to the topics file
            String topicsFilePath = "/home/azureuser/lucene-search-engine/twentythree/topics";
            File topicsFile = new File(topicsFilePath);

            // Output file for top 1000 results in TREC format
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("top1000_results_topics.txt"))) {
                try (BufferedReader br = new BufferedReader(new FileReader(topicsFile))) {
                    String line;
                    String topicId = null;
                    StringBuilder queryBuilder = new StringBuilder();
                    boolean insideDesc = false; // Flag to track whether inside a <desc> block

                    while ((line = br.readLine()) != null) {
                        line = line.trim();

                        if (line.startsWith("<num>")) {
                            // Extract topic ID
                            topicId = line.replace("<num>", "").replace("Number:", "").trim();
                        } else if (line.startsWith("<title>")) {
                            // Extract title as the main query
                            queryBuilder.append(line.replace("<title>", "").trim()).append(" ");
                        } else if (line.startsWith("<desc>")) {
                            // Start of description block
                            insideDesc = true;
                            queryBuilder.append(line.replace("<desc>", "").replace("Description:", "").trim()).append(" ");
                        } else if (line.startsWith("</top>")) {
                            // End of topic, process the query
                            insideDesc = false; // Reset the flag
                            if (topicId != null && queryBuilder.length() > 0) {
                                String queryText = queryBuilder.toString().trim();
                                queryBuilder.setLength(0); // Clear the builder for the next topic

                                System.out.printf("Querying Topic %s: %s%n", topicId, queryText);

                                // Parse and execute the query
                                Query query = parser.parse(QueryParserBase.escape(queryText));
                                TopDocs results = searcher.search(query, 1000); // Top 1000 results

                                // Write results in TREC format
                                for (int i = 0; i < results.scoreDocs.length; i++) {
                                    ScoreDoc scoreDoc = results.scoreDocs[i];
                                    Document doc = searcher.doc(scoreDoc.doc);
                                    String docNo = doc.get("docno");
                                    float score = scoreDoc.score;

                                    writer.write(String.format("%s Q0 %s %d %.4f Lucene-Search\n", topicId, docNo, i + 1, score));
                                }
                            }
                        } else if (insideDesc) {
                            // Append subsequent lines of the description
                            queryBuilder.append(line).append(" ");
                        }
                    }
                }

                System.out.println("\nSearch complete. Results saved to 'top1000_results_topics.txt'.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}