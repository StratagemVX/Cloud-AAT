package com.chatapp.controller;

import com.chatapp.dto.MessageDTO;
import com.chatapp.service.ChatService;
import com.chatapp.service.ChatbotService;
import com.chatapp.service.UserService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Handles real-time WebSocket chat messages and REST chat-history queries.
 *
 * <h3>STOMP Destinations</h3>
 * <ul>
 * <li>{@code /app/chat.sendMessage} → processes &amp; broadcasts a message</li>
 * <li>{@code /app/chat.addUser} → registers a new user and announces join</li>
 * <li>{@code /topic/public} → broadcast destination all clients subscribe
 * to</li>
 * </ul>
 */
@RestController
public class ChatController {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final ChatService chatService;
    private final ChatbotService chatbotService;
    private final UserService userService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(ChatService chatService,
            ChatbotService chatbotService,
            UserService userService,
            SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.chatbotService = chatbotService;
        this.userService = userService;
        this.messagingTemplate = messagingTemplate;
    }

    // ═══════════════════════════════════════════════════════════════════
    // WebSocket Handlers
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Handle an incoming chat message.
     *
     * <p>
     * If the message starts with {@code @bot}, the content after the prefix
     * is forwarded to the {@link ChatbotService}, and the bot's response is
     * broadcast to all subscribers.
     * </p>
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageDTO messageDTO) {
        // Persist the user's message
        messageDTO.setType("CHAT");
        messageDTO.setTimestamp(LocalDateTime.now().format(FMT));
        chatService.saveMessage(messageDTO);

        // Broadcast the user's message FIRST
        messagingTemplate.convertAndSend("/topic/public", messageDTO);

        // ── Chatbot auto-reply to every message ────────────────────────
        String content = messageDTO.getContent();
        if (content != null && !content.isBlank()) {
            // If prefixed with @bot, strip the prefix; otherwise use full content
            String query;
            if (content.toLowerCase().startsWith("@bot")) {
                query = content.substring(4).trim();
            } else {
                query = content.trim();
            }

            if (!query.isEmpty()) {
                String botReply = chatbotService.getResponse(query);

                // Build and broadcast the bot's reply AFTER the user message
                MessageDTO botMessage = new MessageDTO(
                        "ChatBot", botReply, "BOT",
                        LocalDateTime.now().format(FMT));
                chatService.saveMessage(botMessage);

                messagingTemplate.convertAndSend("/topic/public", botMessage);
            }
        }
    }

    /**
     * Handle a new user joining the chat room.
     *
     * <p>
     * Stores the username in the WebSocket session attributes so that
     * the {@link WebSocketEventListener} can detect disconnections.
     * </p>
     */
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public MessageDTO addUser(@Payload MessageDTO messageDTO,
            SimpMessageHeaderAccessor headerAccessor) {
        // Register the user (creates or re-activates)
        userService.connectUser(messageDTO.getSender());

        // Store username in WebSocket session for disconnect detection
        headerAccessor.getSessionAttributes().put("username", messageDTO.getSender());

        messageDTO.setType("JOIN");
        messageDTO.setContent(messageDTO.getSender() + " joined the chat!");
        messageDTO.setTimestamp(LocalDateTime.now().format(FMT));
        chatService.saveMessage(messageDTO);
        return messageDTO;
    }

    // ═══════════════════════════════════════════════════════════════════
    // REST Endpoints
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Return the 50 most recent chat messages (for new clients to load history).
     */
    @GetMapping("/api/messages")
    public List<MessageDTO> getMessageHistory() {
        return chatService.getRecentMessages();
    }

    /**
     * Delete all chat messages (clear history).
     */
    @org.springframework.web.bind.annotation.DeleteMapping("/api/messages")
    public org.springframework.http.ResponseEntity<Void> clearHistory() {
        chatService.clearHistory();
        return org.springframework.http.ResponseEntity.noContent().build();
    }
}
