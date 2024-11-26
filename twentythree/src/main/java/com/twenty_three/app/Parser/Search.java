package com.twenty_three.app.Parser;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.queries.mlt.MoreLikeThisQuery;
import com.twenty_three.app.Parser.CustomAnalyzer;

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
            // Get the index path and topics file path from command-line arguments or default values
            String indexPath =  "index";
            String topicsFilePath = "/vol/bitbucket/ss8923/lucene-search-engine/twentythree/topics";

            // Open the Lucene index
            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            IndexSearcher searcher = new IndexSearcher(reader);

            // Set BM25 as the similarity model
            searcher.setSimilarity(new BM25Similarity());

            // Use the EnglishAnalyzer for querying
            CustomAnalyzer analyzer = new CustomAnalyzer();
            // Dynamic field weighting (ensure field names match the index)
            Map<String, Float> boosts = new HashMap<>();
            boosts.put("title", 0.1f);
            boosts.put("content", 0.9f);

            MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"title", "content"}, analyzer, boosts);

            // Output file for top 1000 results in TREC format
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("top1000_results_topics.txt"))) {
                try (BufferedReader br = new BufferedReader(new FileReader(topicsFilePath))) {
                    String line;
                    String topicId = null;
                    StringBuilder queryBuilder = new StringBuilder();
                    boolean insideDesc = false;

                    while ((line = br.readLine()) != null) {
                        line = line.trim();

                        if (line.startsWith("<num>")) {
                            topicId = line.replace("<num>", "").replace("Number:", "").trim();
                        } else if (line.startsWith("<title>")) {
                            queryBuilder.append(line.replace("<title>", "").trim()).append(" ");
                        } else if (line.startsWith("<desc>")) {
                            insideDesc = true;
                            queryBuilder.append(line.replace("<desc>", "").replace("Description:", "").trim()).append(" ");
                        } else if (line.startsWith("</top>")) {
                            insideDesc = false;

                            // Validate topic ID and query before proceeding
                            if (topicId != null && queryBuilder.length() > 0) {
                                String queryText = queryBuilder.toString().trim();
                                queryBuilder.setLength(0); // Reset query builder for next topic

                                System.out.printf("Querying Topic %s: %s%n", topicId, queryText);

                                // Base query
                                Query baseQuery = parser.parse(QueryParserBase.escape(queryText));
                                TopDocs baseResults = searcher.search(baseQuery, 1000);

                                // Query Expansion
                                Query expandedQuery = expandQuery(searcher, analyzer, baseQuery, reader);

                                // Search with expanded query
                                TopDocs expandedResults = searcher.search(expandedQuery, 1000);

                                // Write results in TREC format
                                for (int i = 0; i < expandedResults.scoreDocs.length; i++) {
                                    ScoreDoc scoreDoc = expandedResults.scoreDocs[i];
                                    Document doc = searcher.doc(scoreDoc.doc);
                                    String docNo = doc.get("docno");

                                    // Validate docNo before writing
                                    if (docNo != null && !docNo.isEmpty()) {
                                        float score = scoreDoc.score;
                                        writer.write(String.format("%s Q0 %s %d %.4f Expanded\n", topicId, docNo, i + 1, score));
                                    }
                                }
                            }
                        } else if (insideDesc) {
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

    /**
     * Expands a query using `MoreLikeThisQuery` for relevance feedback.
     *
     * @param searcher The IndexSearcher instance
     * @param analyzer The Analyzer to use
     * @param baseQuery The original query
     * @param reader The IndexReader instance
     * @return The expanded query
     * @throws IOException If an error occurs during query expansion
     */
    private static Query expandQuery(IndexSearcher searcher, Analyzer analyzer, Query baseQuery, IndexReader reader) throws IOException {
        BooleanQuery.Builder expandedQueryBuilder = new BooleanQuery.Builder();
        expandedQueryBuilder.add(baseQuery, BooleanClause.Occur.SHOULD); // Original query

        // Retrieve top 4 documents for query expansion
        TopDocs topDocs = searcher.search(baseQuery, 4);

        for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
            Document doc = reader.document(scoreDoc.doc);
            String content = doc.get("content");

            // Validate content before using it
            if (content != null && !content.trim().isEmpty()) {
                MoreLikeThisQuery mltQuery = new MoreLikeThisQuery(content, new String[]{"content"}, analyzer, "content");
                Query rewrittenQuery = mltQuery.rewrite(reader);

                // Add the rewritten query to the expanded query
                expandedQueryBuilder.add(rewrittenQuery, BooleanClause.Occur.SHOULD);
            }
        }

        return expandedQueryBuilder.build();
    }
}