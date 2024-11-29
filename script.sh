#!/bin/bash

# Path to the JAR file
JAR_PATH="twentythree/target/twentythree-1.0-SNAPSHOT.jar"
# TREC evaluation tool path
TREC_EVAL_PATH="../trec_eval/trec_eval"

# QREL file path
QREL_PATH="twentythree/qrels.assignment2.part1"
INDEX_PATH="/vol/bitbucket/ss8923/lucene-search-engine/twentythree/index"
# Path to the results file from Java
JAVA_RESULTS_FILE="/vol/bitbucket/ss8923/lucene-search-engine/twentythree/top1000_results_topics.txt"

# Output directory for results
RESULT_DIR="/vol/bitbucket/ss8923/lucene-search-engine/twentythree/results"
mkdir -p "$RESULT_DIR"
 #"EnglishAnalyzer"
   # "ClassicAnalyzer"
    #"SimpleAnalyzer"
    #"WhitespaceAnalyzer"
    #"KeywordAnalyzer"

    # "ClassicSimilarity" "BooleanSimilarity"

# List of analyzers
ANALYZERS=(
    "StandardAnalyzer"
)

# List of scoring methods
SCORING_METHODS=("BM25Similarity")

# Iterate through each combination of analyzers and scoring methods
for ANALYZER in "${ANALYZERS[@]}"; do
    for SCORING in "${SCORING_METHODS[@]}"; do
        rm -rf "$INDEX_PATH"
        rm -f "$JAVA_RESULTS_FILE"
        # Generate a unique result file name for this combination
        OUTPUT_FILE="${ANALYZER}_${SCORING}_results.txt"

        # Run the Java program with the specified analyzer and scoring method
        echo "Running with Analyzer: $ANALYZER, Scoring: $SCORING"
        
        java -jar "$JAR_PATH" "$ANALYZER" "$SCORING" 
        $TREC_EVAL_PATH "$QREL_PATH" "$JAVA_RESULTS_FILE" > "$OUTPUT_FILE"

        # Check if the run was successful
        if [ $? -eq 0 ]; then
            echo "Results saved to $OUTPUT_FILE"
        else
            echo "Error running with Analyzer: $ANALYZER, Scoring: $SCORING"
        fi
       
    done
done

echo "All combinations have been executed."
