#!/bin/bash

# Path to the JAR file
JAR_PATH="target/twentythree-1.0-SNAPSHOT.jar"

# Output directory for results
RESULT_DIR="results"
mkdir -p "$RESULT_DIR"

# List of analyzers
ANALYZERS=(
    "StandardAnalyzer"
    "EnglishAnalyzer"
    "ClassicAnalyzer"
    "SimpleAnalyzer"
    "WhitespaceAnalyzer"
    "FrenchAnalyzer"
    "GermanAnalyzer"
    "SpanishAnalyzer"
    "KeywordAnalyzer"
    "StopAnalyzer"
    "LowerCaseAnalyzer"
)

# List of scoring methods
SCORING_METHODS=("BM25Similarity" "ClassicSimilarity" "BooleanSimilarity")

# Iterate through each combination of analyzers and scoring methods
for ANALYZER in "${ANALYZERS[@]}"; do
    for SCORING in "${SCORING_METHODS[@]}"; do
        # Generate a unique result file name for this combination
        OUTPUT_FILE="${RESULT_DIR}/${ANALYZER}_${SCORING}_results.txt"

        # Run the Java program with the specified analyzer and scoring method
        echo "Running with Analyzer: $ANALYZER, Scoring: $SCORING"
        java -jar "$JAR_PATH" "$ANALYZER" "$SCORING" > "$OUTPUT_FILE"

        # Check if the run was successful
        if [ $? -eq 0 ]; then
            echo "Results saved to $OUTPUT_FILE"
        else
            echo "Error running with Analyzer: $ANALYZER, Scoring: $SCORING"
        fi
    done
done

echo "All combinations have been executed."
