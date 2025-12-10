import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Project2: A Java-based text processing system for word frequency analysis,
 * using built-in Java Collections for efficient data handling.
 *
 */
public class Harriott_project2 {

    // --- Data Structures to Store Results ---
    private static Set<String> stopwords = new HashSet<>();
    private static Map<String, Integer> wordFrequencies = new HashMap<>();
    private static long totalTokenCount = 0;
    private static long totalCharacters = 0;

    // --- File Paths (Assumes files are in the same directory as the code) ---
    private static final String STOPWORDS_FILE = "stopwords.txt";
    private static final String TEXT_FILE = "alice29.txt";

    public static void main(String[] args) {
        System.out.println("--- Java Text Processing System ---");

        // Task 1 & Setup: Load Stopwords and Process Text
        try {
            // Load Stopwords (O(S) - S is the number of stopwords)-Ai text description update
            stopwords = loadStopwords(STOPWORDS_FILE);
            System.out.printf("1. Stopwords loaded: %d words.\n", stopwords.size());

            // Process Text and Count Frequencies (O(N) - N is the number of tokens) -Ai text description update
            processTextAndCountFrequencies(TEXT_FILE, stopwords);
            System.out.printf("2. Text processing complete: %d total tokens processed, %d unique words remaining.\n",
                    totalTokenCount, wordFrequencies.size());

            // Check if any words were processed
            if (totalTokenCount == 0) {
                System.out.println("Error: No tokens were processed from the text file.");
                return;
            }

            // Task 3: Finding Top N Words
            final int N = 10;
            System.out.printf("\n--- Task 3: Top %d Words and Ratio Calculation ---\n", N);

            // Find Top N Words (O(M log M) - M is the number of unique words)
            List<Map.Entry<String, Integer>> topNWords = findTopNWords(wordFrequencies, N);
            System.out.printf("Top %d words (Total Unique Words: %d):\n", N, wordFrequencies.size());
            int rank = 1;
            for (Map.Entry<String, Integer> entry : topNWords) {
                System.out.printf("%d. %s: %d\n", rank++, entry.getKey(), entry.getValue());
            }

            // Calculate Ratios (O(N_top) - N_top is 10)
            calculateRatios(wordFrequencies, topNWords, totalTokenCount);

            // Task 4: Additional Functions
            System.out.println("\n--- Task 4: Additional Analysis ---\n");
            performAdditionalAnalysis(wordFrequencies, totalTokenCount, totalCharacters);

        } catch (FileNotFoundException e) {
            System.err.println("Error: One of the input files was not found. " + e.getMessage());
            System.err.println("Please ensure 'alice29.txt' and 'stopwords.txt' are in the project directory.");
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    /**
     * Task 1 Helper: Reads stopwords from a file into a HashSet.
     * The use of HashSet provides O(1) average time complexity for subsequent lookups,
     * which is crucial for efficient stopword removal in the main processing loop.
     *
     * @param filename The path to the stopwords file.
     * @return A HashSet containing all stopwords in lowercase.
     * @throws FileNotFoundException If the file cannot be found.
     * Time Complexity: O(S), where S is the total number of stopwords.
     */
    public static Set<String> loadStopwords(String filename) throws FileNotFoundException {
        Set<String> stopwordSet = new HashSet<>();
        File file = new File(filename);
        Scanner scanner = new Scanner(file);

        while (scanner.hasNext()) {
            // Read word, convert to lowercase, and add to set
            stopwordSet.add(scanner.next().toLowerCase());
        }
        scanner.close();
        return stopwordSet;
    }

    /**
     * Task 1 & 2: Reads the text file, tokenizes, cleans, removes stopwords,
     * and counts word frequencies using a HashMap.
     *
     * @param textFilename The path to the main text file.
     * @param stopwords The set of stopwords for removal.
     * @throws FileNotFoundException If the file cannot be found.
     * Time Complexity: O(N), where N is the total number of tokens in the text.
     * This is due to O(1) average time for both HashSet lookup and HashMap insertion/update.
     */
    public static void processTextAndCountFrequencies(String textFilename, Set<String> stopwords) throws FileNotFoundException {
        File file = new File(textFilename);
        Scanner scanner = new Scanner(file);
        // Use a pattern to find words: sequences of letters, apostrophes, or hyphens (to handle contractions and hyphenated words).
        // This is a robust tokenization method.
        Pattern pattern = Pattern.compile("[a-zA-Z]+(?:['-][a-zA-Z]+)*");

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            Matcher matcher = pattern.matcher(line);

            while (matcher.find()) {
                String token = matcher.group().toLowerCase();

                // Tokenization, lowercasing, and removal of non-alphanumeric chars happen implicitly by the regex and .toLowerCase()

                // Stopword removal check (O(1) average)
                if (!stopwords.contains(token)) {
                    // Frequency count (O(1) average)
                    wordFrequencies.put(token, wordFrequencies.getOrDefault(token, 0) + 1);
                    totalTokenCount++;
                    totalCharacters += token.length();
                }
            }
        }
        scanner.close();
    }

    /**
     * Task 3: Finds the top N most frequent words by sorting the HashMap entries.
     *
     * @param wordFrequencies The map containing word counts.
     * @param n The number of top words to return.
     * @return A list of Map.Entry objects representing the top N words.
     * Time Complexity: O(M log M), where M is the number of unique words (wordFrequencies.size()).
     * This is dominated by the sorting operation (Collections.sort).
     */
    public static List<Map.Entry<String, Integer>> findTopNWords(Map<String, Integer> wordFrequencies, int n) {
        // Convert Map entries to a List
        List<Map.Entry<String, Integer>> list = new ArrayList<>(wordFrequencies.entrySet());

        // Sort the list based on word frequency (value) in descending order
        // This is the O(M log M) operation
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                // Descending order of frequency
                return o2.getValue().compareTo(o1.getValue());
            }
        });

        // Return the first N entries (or all, if less than N)
        return list.subList(0, Math.min(n, list.size()));
    }

    /**
     * Task 3: Calculates and prints the ratio of the total count of the top 10
     * words to the total count of all processed tokens.
     *
     * @param wordFrequencies The map containing all word counts.
     * @param topNWords A list of the top N word entries.
     * @param totalTokenCount The total count of all non-stopword tokens.
     * Time Complexity: O(N_top), where N_top is the number of top words (e.g., 10).
     */
    public static void calculateRatios(Map<String, Integer> wordFrequencies, List<Map.Entry<String, Integer>> topNWords, long totalTokenCount) {
        long topNCount = 0;
        for (Map.Entry<String, Integer> entry : topNWords) {
            topNCount += entry.getValue();
        }

        // Calculate ratios
        double ratioTopNToTotal = (double) topNCount / totalTokenCount;

        System.out.printf("\nCalculation Results:\n");
        System.out.printf("Total Processed Tokens (Post-Stopword Removal): %d\n", totalTokenCount);
        System.out.printf("Total Count of Top %d Words: %d\n", topNWords.size(), topNCount);
        System.out.printf("Ratio (Top %d Words / Total Tokens): %.6f\n", topNWords.size(), ratioTopNToTotal);
    }

    /**
     * Task 4: Performs additional analysis, calculating the unique word count
     * and the average word length of the processed text.
     *
     * @param wordFrequencies The map containing all word counts.
     * @param totalTokenCount The total count of all non-stopword tokens.
     * @param totalCharacters The total character count of all non-stopword tokens.
     * Time Complexity: O(1) for unique count; O(1) for average word length (using pre-calculated totals).
     */
    public static void performAdditionalAnalysis(Map<String, Integer> wordFrequencies, long totalTokenCount, long totalCharacters) {
        // Unique Word Count (O(1) lookup on the map)
        long uniqueWordCount = wordFrequencies.size();

        // Average Word Length (O(1) calculation using accumulated totals)
        double averageWordLength = (totalTokenCount > 0) ? (double) totalCharacters / totalTokenCount : 0.0;

        System.out.printf("Unique Word Count (After Preprocessing): %d\n", uniqueWordCount);
        System.out.printf("Total Characters in Processed Text: %d\n", totalCharacters);
        System.out.printf("Average Word Length: %.4f characters\n", averageWordLength);
    }
}
