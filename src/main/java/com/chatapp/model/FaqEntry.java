package com.chatapp.model;

import jakarta.persistence.*;

/**
 * JPA entity representing a Frequently Asked Question entry.
 *
 * <p>
 * Seeded from {@code data.sql}. The chatbot service compares
 * user queries against the {@code question} field using TF-IDF
 * cosine similarity, and returns the matching {@code answer}.
 * </p>
 */
@Entity
@Table(name = "faq_entries")
public class FaqEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The question text used for TF-IDF matching. */
    @Column(nullable = false, length = 500)
    private String question;

    /** The answer returned to the user when the question matches. */
    @Column(nullable = false, length = 2000)
    private String answer;

    /** Organizational category (e.g. "general", "features"). */
    @Column(length = 100)
    private String category;

    /** Comma-separated keywords/synonyms for enriched TF-IDF matching. */
    @Column(length = 1000)
    private String keywords;

    // ─── Constructors ──────────────────────────────────────────────────
    public FaqEntry() {
    }

    public FaqEntry(String question, String answer, String category, String keywords) {
        this.question = question;
        this.answer = answer;
        this.category = category;
        this.keywords = keywords;
    }

    // ─── Getters / Setters ─────────────────────────────────────────────
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }
}
