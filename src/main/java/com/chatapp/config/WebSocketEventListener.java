package com.chatapp.config;

import com.chatapp.dto.MessageDTO;
import com.chatapp.service.UserService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Listens for WebSocket session events (disconnect) to update
 * user presence and broadcast leave notifications.
 */
@Component
public class WebSocketEventListener {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final SimpMessageSendingOperations messagingTemplate;
    private final UserService userService;

    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate,
            UserService userService) {
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
    }

    /**
     * Triggered when a WebSocket client disconnects.
     * Marks the user OFFLINE and broadcasts a LEAVE message.
     */
    @EventListener
    public void handleWebSocketDisconnect(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        if (username != null) {
            // Update user status in the database
            userService.disconnectUser(username);

            // Broadcast leave event to all subscribers
            MessageDTO leaveMessage = new MessageDTO(
                    username,
                    username + " left the chat!",
                    "LEAVE",
                    LocalDateTime.now().format(FMT));
            messagingTemplate.convertAndSend("/topic/public", leaveMessage);
        }
    }
}
