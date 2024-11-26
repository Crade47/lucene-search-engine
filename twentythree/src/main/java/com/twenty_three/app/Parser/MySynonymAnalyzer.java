package com.twenty_three.app.Parser;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.en.EnglishMinimalStemFilter;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.analysis.synonym.WordnetSynonymParser;

import java.io.FileReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class MySynonymAnalyzer extends Analyzer {

    private final CharArraySet stopwords;
    private final CharArraySet keywords;
    private SynonymMap synonymMap;

    public static final CharArraySet CustomWordSet = new CharArraySet(
            List.of("i",
			"me",
			"my",
			"myself",
			"we",
			"our",
			"ours",
			"ourselves",
			"you",
			"your",
			"yours",
			"yourself",
			"yourselves",
			"he",
			"him",
			"his",
			"himself",
			"she",
			"her",
			"hers",
			"herself",
			"it",
			"its",
			"itself",
			"they",
			"them",
			"their",
			"theirs",
			"themselves",
			"what",
			"which",
			"who",
			"whom",
			"this",
			"that",
			"these",
			"those",
			"am",
			"is",
			"are",
			"was",
			"were",
			"be",
			"been",
			"being",
			"have",
			"has",
			"had",
			"having",
			"do",
			"does",
			"did",
			"doing",
			"a",
			"an",
			"the",
			"and",
			"but",
			"if",
			"or",
			"because",
			"as",
			"until",
			"while",
			"of",
			"at",
			"by",
			"for",
			"with",
			"about",
			"against",
			"between",
			"into",
			"through",
			"during",
			"before",
			"after",
			"above",
			"below",
			"to",
			"from",
			"up",
			"down",
			"in",
			"out",
			"on",
			"off",
			"over",
			"under",
			"again",
			"further",
			"then",
			"once",
			"here",
			"there",
			"when",
			"where",
			"why",
			"how",
			"all",
			"any",
			"both",
			"each",
			"few",
			"more",
			"most",
			"other",
			"some",
			"such",
			"no",
			"nor",
			"not",
			"only",
			"own",
			"same",
			"so",
			"than",
			"too",
			"very",
			"s",
			"t",
			"can",
			"will",
			"just",
			"don",
			"should",
			"now"), true);

    public MySynonymAnalyzer() {
        this.stopwords = CustomWordSet;
        this.keywords = CharArraySet.EMPTY_SET;
        parseSynonyms();
    }

    public MySynonymAnalyzer(CharArraySet stopwords) {
        this.stopwords = stopwords;
        this.keywords = CharArraySet.EMPTY_SET;
        parseSynonyms();
    }

    public MySynonymAnalyzer(CharArraySet stopwords, CharArraySet keywords) {
        this.stopwords = stopwords;
        this.keywords = keywords;
        parseSynonyms();
    }

    public MySynonymAnalyzer(String[] stopwords, String[] keywords) {
        this.stopwords = new CharArraySet(Arrays.asList(stopwords), true);
        this.keywords = new CharArraySet(Arrays.asList(keywords), true);
        parseSynonyms();
    }

    /**
     * Parses the WordNet synonym file to build the SynonymMap.
     */
    private void parseSynonyms() {
        try {
            String synonymFilePath = Paths.get("/vol/bitbucket/ss8923/lucene-search-engine/twentythree/wn_s.pl").toAbsolutePath().toString();
            WordnetSynonymParser parser = new WordnetSynonymParser(true, true, new StandardAnalyzer());

            // Read the WordNet file
            try (FileReader fileReader = new FileReader(synonymFilePath)) {
                parser.parse(fileReader);
            }

            this.synonymMap = parser.build();
        } catch (Exception e) {
            e.printStackTrace();
            this.synonymMap = null; // Fallback in case of error
        }
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer tokenizer = new StandardTokenizer();

        TokenStream tokenStream = new LowerCaseFilter(tokenizer);
        tokenStream = new TrimFilter(tokenStream);
        tokenStream = new EnglishPossessiveFilter(tokenStream);

        if (!this.keywords.isEmpty()) {
            tokenStream = new SetKeywordMarkerFilter(tokenStream, this.keywords);
        }

        tokenStream = new EnglishMinimalStemFilter(tokenStream);
        tokenStream = new StopFilter(tokenStream, this.stopwords);

        if (this.synonymMap != null) {
            tokenStream = new SynonymGraphFilter(tokenStream, this.synonymMap, true);
        }

        tokenStream = new PorterStemFilter(tokenStream);

        return new TokenStreamComponents(tokenizer, tokenStream);
    }
}
