package com.twenty_three.app.Parser;

import org.apache.lucene.analysis.*;
import org.apache.lucene.analysis.classic.ClassicTokenizer;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.core.LetterTokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.en.PorterStemFilter;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.miscellaneous.TrimFilter;
import org.apache.lucene.analysis.pattern.PatternReplaceFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.synonym.SynonymGraphFilter;
import org.apache.lucene.analysis.synonym.SynonymMap;
import org.apache.lucene.util.CharsRef;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;

public final class MySynonymAnalyzer extends StopwordAnalyzerBase {

    public static final CharArraySet ENGLISH_STOP_WORDS_SET;

    static {
        final String[] stopWords = {
            // Add your stop words here
        };
        ENGLISH_STOP_WORDS_SET = CharArraySet.unmodifiableSet(new CharArraySet(Arrays.asList(stopWords), false));
    }

    private final CharArraySet stemExclusionSet;
    private final SynonymMap synonymMap;

    public MySynonymAnalyzer(String synonymFilePath) throws IOException {
        this(ENGLISH_STOP_WORDS_SET, synonymFilePath);
    }

    public MySynonymAnalyzer(CharArraySet stopwords, String synonymFilePath) throws IOException {
        this(stopwords, CharArraySet.EMPTY_SET, synonymFilePath);
    }

    public MySynonymAnalyzer(CharArraySet stopwords, CharArraySet stemExclusionSet, String synonymFilePath) throws IOException {
        super(stopwords);
        this.stemExclusionSet = CharArraySet.unmodifiableSet(CharArraySet.copy(stemExclusionSet));
        this.synonymMap = buildSynonymMap(synonymFilePath);
    }

    private SynonymMap buildSynonymMap(String filePath) throws IOException {
        SynonymMap.Builder builder = new SynonymMap.Builder(true);
        Map<Integer, Set<String>> synonymGroups = new HashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.startsWith("s(") || !line.endsWith(").")) {
                    continue; // Skip invalid lines
                }
                // Parse line: s(ID,SENSE,'TERM',POS,FREQ,FLAG).
                String[] parts = line.substring(2, line.length() - 2).split(",");
                int id = Integer.parseInt(parts[0]);
                String term = parts[2].replace("'", "").trim();

                synonymGroups.putIfAbsent(id, new HashSet<>());
                synonymGroups.get(id).add(term);
            }
        }

        // Build synonym map
        for (Set<String> synonyms : synonymGroups.values()) {
            for (String input : synonyms) {
                for (String output : synonyms) {
                    if (!input.equals(output)) {
                        builder.add(new CharsRef(input), new CharsRef(output), true);
                    }
                }
            }
        }
        return builder.build();
    }

    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer source = new StandardTokenizer();
        TokenStream result = new EnglishPossessiveFilter(source);
        result = new LowerCaseFilter(result);

        // Add a filter to remove special characters
        Pattern specialCharPattern = Pattern.compile("[?/\\-()]");
        result = new PatternReplaceFilter(result, specialCharPattern, "", true);

        result = new StopFilter(result, stopwords);

        if (!stemExclusionSet.isEmpty()) {
            result = new SetKeywordMarkerFilter(result, stemExclusionSet);
        }

        result = new SynonymGraphFilter(result, synonymMap, true);
        result = new PorterStemFilter(result);
        return new TokenStreamComponents(source, result);
    }

    @Override
    protected TokenStream normalize(String fieldName, TokenStream in) {
        return new LowerCaseFilter(in);
    }
}
