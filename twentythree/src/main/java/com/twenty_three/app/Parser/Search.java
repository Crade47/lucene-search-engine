package com.twenty_three.app.Parser;

import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.classic.QueryParserBase;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.queries.mlt.MoreLikeThisQuery;

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
           
            String indexPath = "index";
            String topicsFilePath ="topics";

            IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            IndexSearcher searcher = new IndexSearcher(reader);
            searcher.setSimilarity(new BM25Similarity());

            EnglishAnalyzer analyzer = new EnglishAnalyzer();
           // MySynonymAnalyzer analyzer = new MySynonymAnalyzer("/vol/bitbucket/ss8923/lucene-search-engine/twentythree/wn_s.pl");
            Map<String, Float> boosts = new HashMap<>();
            boosts.put("title", 0.08f);
            boosts.put("content", 0.98f);

            MultiFieldQueryParser parser = new MultiFieldQueryParser(new String[]{"title", "content"}, analyzer, boosts);//

            try (BufferedWriter writer = new BufferedWriter(new FileWriter("top1000_results_topics.txt"))) {
                try (BufferedReader br = new BufferedReader(new FileReader(topicsFilePath))) {
                    processTopics(br, parser, searcher, analyzer, reader, writer);
                }
            }

            System.out.println("\nSearch complete. Results saved to 'top1000_results_topics.txt'.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void processTopics(BufferedReader br, MultiFieldQueryParser parser, 
                                       IndexSearcher searcher, Analyzer analyzer, 
                                       IndexReader reader, BufferedWriter writer) throws Exception {
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
                } else if (line.startsWith("<narr>")) {
                        insideDesc = true; // treat narrative as part of the extended description
                        queryBuilder.append(line.replace("<narr>", "").replace("Narrative:", "").trim()).append(" ");
                } else if (line.startsWith("</top>")) {
                        insideDesc = false;

                        if (topicId != null && queryBuilder.length() > 0) {
                                String queryText = queryBuilder.toString().trim();
                                queryBuilder.setLength(0);

                                System.out.printf("Querying Topic %s: %s%n", topicId, queryText);

                                Query origQ = parser.parse(QueryParserBase.escape(queryText));
                                TopDocs baseResults = searcher.search(origQ, 1000);

                                Query optQue = optimiseQuery(searcher, analyzer, origQ, reader);
                                TopDocs optQueResults = searcher.search(optQue, 1000);

                                for (int i = 0; i < optQueResults.scoreDocs.length; i++) {
                                        ScoreDoc scoreDoc = optQueResults.scoreDocs[i];
                                        Document doc = searcher.doc(scoreDoc.doc);
                                        String docNo = doc.get("docno");

                                        if (docNo != null && !docNo.isEmpty()) {
                                                float score = scoreDoc.score;
                                                writer.write(String.format("%s Q0 %s %d %.4f EnglishAnalyzer\n", topicId, docNo, i + 1, score));
                                        }
                                }
                        }
                } else if (insideDesc) {
                        queryBuilder.append(line).append(" ");
                }
        }
    }

    private static Query optimiseQuery(IndexSearcher searcher, Analyzer analyzer, Query inputQuery, IndexReader reader) throws IOException {
        BooleanQuery.Builder optQue = new BooleanQuery.Builder();
        optQue.add(inputQuery, BooleanClause.Occur.SHOULD);

        int maxDocs = 3;
        TopDocs relevantDocs = searcher.search(inputQuery, maxDocs);

        for (ScoreDoc resultDoc : relevantDocs.scoreDocs) {
            String docContent = reader.document(resultDoc.doc).get("content");
            if (docContent != null && !docContent.trim().isEmpty()) {
                MoreLikeThisQuery moreQuery = new MoreLikeThisQuery(
                    docContent,
                    new String[]{"content"},
                    analyzer,
                    "content"
                );
                Query modifiedQuery = moreQuery.rewrite(reader);
                optQue.add(modifiedQuery, BooleanClause.Occur.SHOULD);
            }
        }

        return optQue.build();
    }

    private static void writeResults(BufferedWriter writer, String topicId, TopDocs results, IndexSearcher searcher) throws Exception {
        for (int i = 0; i < results.scoreDocs.length; i++) {
            ScoreDoc scoreDoc = results.scoreDocs[i];
            Document doc = searcher.doc(scoreDoc.doc);
            String docNo = doc.get("docno");

            if (docNo != null && !docNo.isEmpty()) {
                float score = scoreDoc.score;
                writer.write(String.format("%s Q0 %s %d %.4f EnglishAnalyzer\n", topicId, docNo, i + 1, score));
            }
        }
    }
}
