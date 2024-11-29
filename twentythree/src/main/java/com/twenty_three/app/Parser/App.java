package com.twenty_three.app.Parser;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.classic.ClassicAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.BooleanSimilarity;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import com.twenty_three.app.Parser.MySynonymAnalyzer;;

/**
 * Entry point for the application
 */
public class App {
    public static void main(String[] args) {
        // if (args.length < 2) {
        //     System.out.println("Usage: java -jar target/twentythree-1.0-SNAPSHOT.jar <analyzer> <similarity>");
        //     System.exit(1);
        // }

        // String analyzerChoice = args[0];
        // String similarityChoice = args[1];

        // Analyzer analyzer = null;
        // Similarity similarity = null;

        // // Select the analyzer based on the input argument
        // switch (analyzerChoice) {
        //     case "StandardAnalyzer":
        //         analyzer = new StandardAnalyzer();
        //         break;
        //     case "EnglishAnalyzer":
        //         analyzer = new EnglishAnalyzer();
        //         break;
        //     case "ClassicAnalyzer":
        //         analyzer = new ClassicAnalyzer();
        //         break;
        //     case "SimpleAnalyzer":
        //         analyzer = new SimpleAnalyzer();
        //     break;
        //     case "WhitespaceAnalyzer":
        //         analyzer = new WhitespaceAnalyzer();
        //     break;
        //     case "KeywordAnalyzer":
        //         analyzer = new KeywordAnalyzer();
        //     break;
        //     case "custom":
        //         try {
        //             analyzer = new MySynonymAnalyzer("/vol/bitbucket/ss8923/lucene-search-engine/twentythree/wn_s.pl");
        //         } catch (Exception e) {
        //             System.err.println("Error initializing custom analyzer: " + e.getMessage());
        //             System.exit(1);
        //         }
        //         break;
        //     default:
        //         System.err.println("Invalid analyzer choice. Supported options: standard, english, whitespace, custom");
        //         System.exit(1);
        // }

        // // Select the similarity based on the input argument
        // switch (similarityChoice) {
        //     case "BM25Similarity":
        //         similarity = new BM25Similarity();
        //         break;
        //     case "ClassicSimilarity":
        //         similarity = new ClassicSimilarity();
        //         break;
        //     case "BooleanSimilarity":
        //         similarity = new BooleanSimilarity();
        //         break;
    
        //     default:
        //         System.err.println("Invalid similarity choice. Supported options: bm25, classic, dirichlet, jelinek");
        //         System.exit(1);
        // }

        // Perform indexing and searching
        Analyzer analyzer = new StandardAnalyzer();
        Similarity similarity = new BM25Similarity();
        try {
            Index.index(analyzer, similarity);
            Search.search(analyzer, similarity);
        } catch (Exception e) {
            System.err.println("Error during indexing or searching: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
