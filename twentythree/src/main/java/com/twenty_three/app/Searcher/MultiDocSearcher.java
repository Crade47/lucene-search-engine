package com.twenty_three.app.Searcher;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.ReaderUtil;
import org.apache.lucene.index.StoredFields;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import com.twenty_three.app.Constants;

import java.io.*;
import java.util.*;

public class MultiDocSearcher {
    private static final String INDEX_DIR = Constants.BASE_DIR + "/index"; // index directory path
    private static final String QUERY_FILE = Constants.BASE_DIR + "/topics"; // query file path
    private static final String RESULT_FILE = Constants.BASE_DIR + "/results.txt"; // output file path
    private static final int TOP_N = 1000; // Top N

    public static void query() throws Exception {
        Analyzer analyzer = new EnglishAnalyzer();
        List<IndexSearcher> searchers = new ArrayList<>();

        // todo: Define field sets for each indexer
        List<String[]> fieldSets = new ArrayList<>();
        fieldSets.add(new String[] { "title", "text" }); // Fields for indexer FBIS
        fieldSets.add(new String[] { "title", "text" }); // Fields for indexer FR94
        fieldSets.add(new String[] { "title", "text" }); // Fields for indexer FT
        fieldSets.add(new String[] { "title", "text" }); // Fields for indexer LAT

        // Traverse each subdirectory in the index directory and create an IndexSearcher
        // for each
        File indexDir = new File(INDEX_DIR);
        System.out.println(indexDir.exists());
        for (File subDir : Objects.requireNonNull(indexDir.listFiles())) {
            if (subDir.isDirectory()) {
                Directory directory = FSDirectory.open(subDir.toPath());
                DirectoryReader reader = DirectoryReader.open(directory);
                IndexSearcher searcher = new IndexSearcher(reader);
                searcher.setSimilarity(new BM25Similarity());
                searchers.add(searcher);
            }
        }
        // Parse the query file
        List<QueryEntry> queryEntries = parseQueries(analyzer);

        // Open result file writer
        try (PrintWriter writer = new PrintWriter(new FileWriter(RESULT_FILE))) {
            for (QueryEntry queryEntry : queryEntries) {
                // For each query, search across all IndexSearchers with respective fields and
                // collect results
                PriorityQueue<ScoreDocResult> topDocsQueue = new PriorityQueue<>(
                        TOP_N, Comparator.comparingDouble(sd -> -sd.getScore()));

                for (int i = 0; i < searchers.size(); i++) {
                    IndexSearcher searcher = searchers.get(i);
                    String[] fields = fieldSets.get(i);

                    // Create and parse query for the specific fields of the current indexer
                    MultiFieldQueryParser parser = new MultiFieldQueryParser(fields, analyzer);
                    Query query = parser.parse(queryEntry.getQueryText());

                    TopDocs topDocs = searcher.search(query, TOP_N);
                    for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                        topDocsQueue.add(new ScoreDocResult(scoreDoc, searcher, i));
                        if (topDocsQueue.size() > TOP_N) {
                            topDocsQueue.poll(); // Keep only the top N results
                        }
                    }
                }

                int rank = 1;
                while (!topDocsQueue.isEmpty()) {
                    ScoreDocResult result = topDocsQueue.poll();

                    // Retrieve the document using the recommended API
                    int docID = result.getScoreDoc().doc;
                    IndexSearcher searcher = result.getSearcher();
                    LeafReaderContext leafContext = searcher.getIndexReader().leaves()
                            .get(ReaderUtil.subIndex(docID, searcher.getIndexReader().leaves()));
                    StoredFields storedFields = leafContext.reader().storedFields();
                    Document doc = storedFields.document(docID - leafContext.docBase);

                    // Write the result to the file
                    writer.printf("%s 0 %s %d %.4f STANDARD%n",
                            queryEntry.getQueryNo(),
                            doc.get("docno"),
                            rank,
                            result.getScore());
                    rank++;
                }
            }
        }

        // Close readers for all searchers
        for (IndexSearcher searcher : searchers) {
            searcher.getIndexReader().close();
        }
    }

    // Method to parse query file and create combined query for all document types
    private static List<QueryEntry> parseQueries(Analyzer analyzer) throws IOException {
        List<QueryEntry> queryEntries = new ArrayList<>();
        File queryFile = new File(QUERY_FILE);

        try (BufferedReader reader = new BufferedReader(new FileReader(queryFile))) {
            String line;
            String queryNo = null;
            StringBuilder title = new StringBuilder();
            StringBuilder desc = new StringBuilder();
            StringBuilder narr = new StringBuilder();

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("<num>")) {
                    queryNo = line.replace("<num>", "").replace("Number:", "").trim();
                } else if (line.startsWith("<title>")) {
                    title.append(line.replace("<title>", "").trim()).append(" ");
                } else if (line.startsWith("<desc>")) {
                    while ((line = reader.readLine()) != null && !line.startsWith("<narr>")) {
                        desc.append(line.trim()).append(" ");
                    }
                    if (line.startsWith("<narr>")) {
                        while ((line = reader.readLine()) != null && !line.startsWith("</top>")) {
                            narr.append(line.trim()).append(" ");
                        }
                    }
                }

                if (line != null && line.startsWith("</top>")) {
                    String queryText = QueryParser.escape(title.toString().trim() + " " +
                            desc.toString().trim() + " " +
                            narr.toString().trim());
                    queryEntries.add(new QueryEntry(queryNo, queryText));

                    title.setLength(0);
                    desc.setLength(0);
                    narr.setLength(0);
                }
            }
        }

        return queryEntries;
    }

}

// Helper class to store query number and query text
class QueryEntry {
    private final String queryNo;
    private final String queryText;

    public QueryEntry(String queryNo, String queryText) {
        this.queryNo = queryNo;
        this.queryText = queryText;
    }

    public String getQueryNo() {
        return queryNo;
    }

    public String getQueryText() {
        return queryText;
    }
}

// Helper class to store ScoreDoc along with its IndexSearcher and score
class ScoreDocResult {
    private final ScoreDoc scoreDoc;
    private final IndexSearcher searcher;
    private final int index;

    public ScoreDocResult(ScoreDoc scoreDoc, IndexSearcher searcher, int index) {
        this.scoreDoc = scoreDoc;
        this.searcher = searcher;
        this.index = index;
    }

    public ScoreDoc getScoreDoc() {
        return scoreDoc;
    }

    public IndexSearcher getSearcher() {
        return searcher;
    }

    public double getScore() {
        return scoreDoc.score;
    }
}
