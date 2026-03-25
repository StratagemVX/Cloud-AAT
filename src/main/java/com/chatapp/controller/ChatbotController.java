package com.chatapp.controller;

import com.chatapp.service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for direct chatbot queries (outside the WebSocket flow).
 *
 * <p>
 * Useful for testing the chatbot or integrating it from non-WebSocket clients.
 * </p>
 */
@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private final ChatbotService chatbotService;

    public ChatbotController(ChatbotService chatbotService) {
        this.chatbotService = chatbotService;
    }

    /**
     * POST /api/chatbot/ask — send a query to the chatbot and receive a response.
     *
     * @param query the question to ask
     * @return JSON with the bot's reply
     */
    @PostMapping("/ask")
    public ResponseEntity<Map<String, String>> ask(@RequestParam String query) {
        String response = chatbotService.getResponse(query);
        return ResponseEntity.ok(Map.of(
                "query", query,
                "response", response));
    }

    /**
     * GET /api/chatbot/random-question — return a random FAQ question.
     *
     * @return JSON with a single "question" field picked randomly from the FAQ list
     */
    @GetMapping("/random-question")
    public ResponseEntity<Map<String, String>> randomQuestion() {
        String question = chatbotService.getRandomQuestion();
        return ResponseEntity.ok(Map.of("question", question));
    }

    /**
     * GET /api/chatbot/questions — return all FAQ questions with their categories.
     *
     * @return JSON array of {question, category} objects for the FAQ panel
     */
    @GetMapping("/questions")
    public ResponseEntity<List<Map<String, String>>> getAllQuestions() {
        return ResponseEntity.ok(chatbotService.getAllQuestions());
    }
}
