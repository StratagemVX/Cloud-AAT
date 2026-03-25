package com.chatapp.service;

import com.chatapp.model.FaqEntry;
import com.chatapp.repository.FaqRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Chatbot Service implementing TF-IDF vectorization and cosine similarity.
 *
 * <h3>How it works</h3>
 * <ol>
 * <li>On startup, all FAQ entries are loaded from the database.</li>
 * <li>Each FAQ's question + keywords are combined into a single "document"
 * for richer term coverage.</li>
 * <li>An IDF dictionary is built across the enriched FAQ corpus.</li>
 * <li>TF-IDF vectors are pre-computed for each enriched FAQ document.</li>
 * <li>When a user query arrives, its TF-IDF vector is computed and compared
 * against all FAQ vectors using cosine similarity.</li>
 * <li>If the highest score exceeds the threshold (0.15), the matching
 * answer is returned; otherwise a fallback response is given.</li>
 * </ol>
 */
@Service
public class ChatbotService {

    private static final Logger log = LoggerFactory.getLogger(ChatbotService.class);

    /**
     * Minimum cosine-similarity score required to accept a match.
     * Lowered from 0.3 to 0.15 to tolerate paraphrased queries.
     */
    private static final double SIMILARITY_THRESHOLD = 0.15;

    /** Fallback reply when no FAQ matches the user's query. */
    private static final String FALLBACK_RESPONSE = "I'm sorry, I don't have an answer for that. " +
            "Try asking about: application features, technology stack, " +
            "how to use the chatbot, or WebSocket architecture.";

    /** Common English stop-words excluded from TF-IDF computation. */
    private static final Set<String> STOP_WORDS = Set.of(
            "a", "an", "the", "is", "it", "of", "in", "to", "and",
            "or", "for", "on", "at", "by", "with", "from", "as",
            "are", "was", "were", "be", "been", "being", "has",
            "have", "had", "do", "does", "did", "will", "would",
            "can", "could", "should", "may", "might", "shall",
            "this", "that", "these", "those", "i", "you", "we",
            "they", "he", "she", "me", "him", "her", "us", "them",
            "my", "your", "our", "their", "its", "what", "which",
            "who", "whom", "how", "when", "where", "why", "not",
            "no", "so", "if", "but", "about", "up", "out", "just",
            "also", "very", "like", "get", "got", "than", "then");

    private final FaqRepository faqRepository;

    /** All FAQ entries loaded in memory for fast matching. */
    private List<FaqEntry> faqEntries = new ArrayList<>();

    /** Pre-computed TF-IDF vectors (question + keywords combined). */
    private List<Map<String, Double>> faqVectors = new ArrayList<>();

    /** IDF values computed over the enriched FAQ corpus. */
    private Map<String, Double> idfMap = new HashMap<>();

    public ChatbotService(FaqRepository faqRepository) {
        this.faqRepository = faqRepository;
    }

    // ═══════════════════════════════════════════════════════════════════
    // @PostConstruct — early debug check (runs BEFORE data.sql!)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Early debug check — this runs during bean initialization.
     * At this point data.sql may NOT have been executed yet,
     * so faqEntries count may be 0. This is expected.
     */
    @PostConstruct
    public void postConstructCheck() {
        List<FaqEntry> earlyCheck = faqRepository.findAll();
        System.out.println("══════════════════════════════════════════════");
        System.out.println("  [PostConstruct] Loaded FAQ entries: " + earlyCheck.size());
        System.out.println("  NOTE: If 0, data.sql has not run yet — this is expected.");
        System.out.println("  The actual TF-IDF model will be built on ApplicationReadyEvent.");
        System.out.println("══════════════════════════════════════════════");
    }

    // ═══════════════════════════════════════════════════════════════════
    // @EventListener(ApplicationReadyEvent) — actual TF-IDF init
    // This runs AFTER data.sql is fully executed
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Load FAQ data and pre-compute the TF-IDF model.
     *
     * <p>
     * Uses {@code ApplicationReadyEvent} instead of {@code @PostConstruct}
     * to guarantee that {@code data.sql} has been fully executed before
     * the TF-IDF index is built.
     * </p>
     */
    @EventListener(ApplicationReadyEvent.class)
    public void init() {
        faqEntries = faqRepository.findAll();

        System.out.println("══════════════════════════════════════════════");
        System.out.println("  [ApplicationReadyEvent] ChatBot TF-IDF Initialization");
        System.out.println("  Loaded FAQ entries: " + faqEntries.size());
        log.info("═══ ChatBot TF-IDF Initialization ═══");
        log.info("Loaded {} FAQ entries from database", faqEntries.size());

        if (faqEntries.isEmpty()) {
            System.out.println("  WARNING: No FAQ entries found! Chatbot will only return fallback.");
            System.out.println("══════════════════════════════════════════════");
            log.warn("No FAQ entries found — chatbot will only return fallback responses");
            return;
        }

        // Print all loaded FAQ questions for verification
        System.out.println("  FAQ entries loaded:");
        for (int i = 0; i < faqEntries.size(); i++) {
            FaqEntry faq = faqEntries.get(i);
            System.out.println("    [" + i + "] " + faq.getQuestion()
                    + " (keywords: " + (faq.getKeywords() != null ? faq.getKeywords() : "none") + ")");
        }

        // ── Build enriched documents: question + keywords combined ──────
        List<List<String>> tokenizedDocs = new ArrayList<>();
        for (FaqEntry faq : faqEntries) {
            // Combine question text with keywords for richer term coverage
            String combined = faq.getQuestion();
            if (faq.getKeywords() != null && !faq.getKeywords().isBlank()) {
                // Replace commas in keywords with spaces so they tokenize properly
                combined += " " + faq.getKeywords().replace(",", " ");
            }
            List<String> tokens = tokenize(combined);
            tokenizedDocs.add(tokens);
            log.debug("FAQ[{}] '{}' -> tokens: {}", faq.getId(), faq.getQuestion(), tokens);
        }

        // ── Compute IDF with smoothing ─────────────────────────────────
        idfMap = computeIDF(tokenizedDocs);
        System.out.println("  Vocabulary size: " + idfMap.size() + " unique terms");
        log.info("Vocabulary size: {} unique terms", idfMap.size());

        // ── Pre-compute TF-IDF vector for each FAQ ─────────────────────
        faqVectors = tokenizedDocs.stream()
                .map(tokens -> computeTFIDF(tokens, idfMap))
                .collect(Collectors.toList());

        System.out.println("  TF-IDF model ready — " + faqVectors.size() + " FAQ vectors computed");
        System.out.println("  Similarity threshold: " + SIMILARITY_THRESHOLD);
        System.out.println("══════════════════════════════════════════════");
        log.info("TF-IDF model ready — {} FAQ vectors computed", faqVectors.size());
        log.info("Similarity threshold: {}", SIMILARITY_THRESHOLD);
        log.info("═══ ChatBot Initialization Complete ═══");
    }

    // ═══════════════════════════════════════════════════════════════════
    // Public API
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Process a user query and return the best-matching FAQ answer,
     * or the fallback response if no match exceeds the threshold.
     *
     * @param userQuery the raw text typed by the user (after stripping @bot prefix)
     * @return the chatbot's response
     */
    public String getResponse(String userQuery) {
        System.out.println("───── Chatbot Query ─────");
        System.out.println("Raw query: '" + userQuery + "'");
        log.info("───── Chatbot Query ─────");
        log.info("Raw query: '{}'", userQuery);

        if (faqEntries.isEmpty()) {
            System.out.println("WARNING: No FAQ entries loaded — returning fallback");
            log.warn("No FAQ entries loaded — returning fallback");
            return FALLBACK_RESPONSE;
        }

        // Step 1: Tokenize the user query
        List<String> queryTokens = tokenize(userQuery);
        System.out.println("Detected tokens: " + queryTokens);
        log.info("Detected tokens: {}", queryTokens);

        if (queryTokens.isEmpty()) {
            System.out.println("No meaningful tokens — returning fallback");
            log.warn("No meaningful tokens after preprocessing — returning fallback");
            return FALLBACK_RESPONSE;
        }

        // Step 2: Compute the TF-IDF vector for the query
        Map<String, Double> queryVector = computeTFIDF(queryTokens, idfMap);

        // Step 3: Compute similarity against all FAQs and find the best match
        double bestScore = -1.0;
        int bestIndex = -1;

        System.out.println("Similarity scores:");
        for (int i = 0; i < faqVectors.size(); i++) {
            double score = cosineSimilarity(queryVector, faqVectors.get(i));
            if (score > 0.0) {
                System.out.println("  FAQ[" + i + "] score=" + String.format("%.4f", score)
                        + " -> '" + faqEntries.get(i).getQuestion() + "'");
                log.info("  FAQ[{}] score={} -> '{}'",
                        i, String.format("%.4f", score),
                        faqEntries.get(i).getQuestion());
            }
            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }

        System.out.println("Best match: FAQ[" + bestIndex + "] with score="
                + String.format("%.4f", bestScore));

        // Step 4: Apply threshold
        if (bestScore >= SIMILARITY_THRESHOLD && bestIndex >= 0) {
            String matchedQuestion = faqEntries.get(bestIndex).getQuestion();
            String answer = faqEntries.get(bestIndex).getAnswer();
            System.out.println("MATCH — intent='" + matchedQuestion + "', score="
                    + String.format("%.4f", bestScore) + " >= threshold=" + SIMILARITY_THRESHOLD);
            log.info("MATCH — intent='{}', score={} >= threshold={}",
                    matchedQuestion, String.format("%.4f", bestScore), SIMILARITY_THRESHOLD);
            return answer;
        }

        System.out.println("NO MATCH — best score " + String.format("%.4f", bestScore)
                + " < threshold " + SIMILARITY_THRESHOLD);
        log.info("NO MATCH — best score {} < threshold {}",
                String.format("%.4f", bestScore), SIMILARITY_THRESHOLD);
        return FALLBACK_RESPONSE;
    }

    /**
     * Return a random FAQ question from the loaded FAQ entries.
     * Falls back to a default string when the FAQ list is empty.
     */
    public String getRandomQuestion() {
        if (faqEntries.isEmpty()) {
            return "How does this application work?";
        }
        int idx = (int) (Math.random() * faqEntries.size());
        return faqEntries.get(idx).getQuestion();
    }

    /**
     * Return all FAQ questions with their categories for the FAQ panel.
     * Only exposes question and category — not the answer or keywords.
     */
    public List<Map<String, String>> getAllQuestions() {
        return faqEntries.stream()
                .map(faq -> {
                    Map<String, String> item = new LinkedHashMap<>();
                    item.put("question", faq.getQuestion());
                    item.put("category", faq.getCategory() != null ? faq.getCategory() : "general");
                    return item;
                })
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════
    // TF-IDF Engine (private helpers)
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Tokenize input text into a list of lowercase, alphabetic-only terms.
     *
     * <p>
     * Processing steps:
     * </p>
     * <ol>
     * <li>Convert to lowercase</li>
     * <li>Remove all non-alphabetic characters (punctuation, digits)</li>
     * <li>Split on whitespace</li>
     * <li>Filter out tokens shorter than 2 characters</li>
     * <li>Remove common English stop-words</li>
     * </ol>
     */
    private List<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }

        return Arrays.stream(
                text.toLowerCase()
                        .replaceAll("[^a-zA-Z]", " ")
                        .split("\\s+"))
                .filter(t -> t.length() > 1)
                .filter(t -> !STOP_WORDS.contains(t))
                .collect(Collectors.toList());
    }

    /**
     * Compute Inverse Document Frequency (IDF) for every term.
     *
     * <p>
     * Uses a smoothed formula:
     * </p>
     * 
     * <pre>
     * IDF(t) = log((N + 1) / (1 + df(t))) + 1
     * </pre>
     *
     * @param documents list of tokenized documents
     * @return map from term to its IDF value
     */
    private Map<String, Double> computeIDF(List<List<String>> documents) {
        Map<String, Double> idf = new HashMap<>();
        int totalDocs = documents.size();

        // Count document frequency for each term
        Map<String, Integer> docFrequency = new HashMap<>();
        for (List<String> doc : documents) {
            new HashSet<>(doc).forEach(term -> docFrequency.merge(term, 1, Integer::sum));
        }

        // Smoothed IDF = log((N + 1) / (1 + df)) + 1
        docFrequency.forEach((term, df) -> idf.put(term, Math.log((double) (totalDocs + 1) / (1.0 + df)) + 1.0));

        return idf;
    }

    /**
     * Compute TF-IDF vector for a single document (list of tokens).
     *
     * <pre>
     * TF(t, d)  = count(t in d) / |d|
     * TF-IDF(t) = TF(t, d) * IDF(t)
     * </pre>
     */
    private Map<String, Double> computeTFIDF(List<String> tokens, Map<String, Double> idf) {
        Map<String, Double> tfidf = new HashMap<>();

        Map<String, Long> termCounts = tokens.stream()
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));

        double docLength = tokens.size();

        termCounts.forEach((term, count) -> {
            double tf = count / docLength;
            double idfValue = idf.getOrDefault(term, 1.0);
            tfidf.put(term, tf * idfValue);
        });

        return tfidf;
    }

    /**
     * Compute cosine similarity between two sparse TF-IDF vectors.
     *
     * <pre>
     * cosine(A, B) = ( A . B ) / ( ||A|| * ||B|| )
     * </pre>
     */
    private double cosineSimilarity(Map<String, Double> vecA, Map<String, Double> vecB) {
        double dotProduct = 0.0;
        for (Map.Entry<String, Double> entry : vecA.entrySet()) {
            if (vecB.containsKey(entry.getKey())) {
                dotProduct += entry.getValue() * vecB.get(entry.getKey());
            }
        }

        double magnitudeA = Math.sqrt(
                vecA.values().stream().mapToDouble(v -> v * v).sum());
        double magnitudeB = Math.sqrt(
                vecB.values().stream().mapToDouble(v -> v * v).sum());

        if (magnitudeA == 0.0 || magnitudeB == 0.0) {
            return 0.0;
        }

        return dotProduct / (magnitudeA * magnitudeB);
    }
}
