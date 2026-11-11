package com.chatapp.controller;

import com.chatapp.config.CorsOriginUtils;
import com.chatapp.dto.MessageDTO;
import com.chatapp.exception.GlobalExceptionHandler;
import com.chatapp.service.ChatService;
import com.chatapp.service.ChatbotService;
import com.chatapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityHardeningTest {

    @Mock
    private ChatService chatService;

    @Mock
    private ChatbotService chatbotService;

    @Mock
    private UserService userService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private ChatController chatController;
    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        chatController = new ChatController(chatService, chatbotService, userService, messagingTemplate);
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void corsOriginParserRejectsWildcardsAndTrimsExplicitOrigins() {
        String[] origins = CorsOriginUtils.parseAllowedOrigins("https://chat.example.com, *,  , http://localhost:5000 ");
        assertArrayEquals(new String[]{"https://chat.example.com", "http://localhost:5000"}, origins);

        String[] fallback = CorsOriginUtils.parseAllowedOrigins("   ");
        assertTrue(fallback.length > 0);
        for (String origin : fallback) {
            assertNotEquals("*", origin);
        }
    }

    @Test
    void sendMessageRejectsUnregisteredSessionAndBindsSenderToSessionUsername() {
        SimpMessageHeaderAccessor emptyAccessor = SimpMessageHeaderAccessor.create();
        emptyAccessor.setSessionAttributes(new HashMap<>());

        MessageDTO unauthDto = new MessageDTO("SpoofedAdmin", "hello", "CHAT");
        assertThrows(IllegalStateException.class, () -> chatController.sendMessage(unauthDto, emptyAccessor));
        verify(chatService, never()).saveMessage(any());

        SimpMessageHeaderAccessor authedAccessor = SimpMessageHeaderAccessor.create();
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("username", "alice");
        authedAccessor.setSessionAttributes(attrs);

        MessageDTO spoofedDto = new MessageDTO("SpoofedAdmin", "  hello world  ", "CHAT");
        when(chatbotService.getResponse("hello world")).thenReturn("hi alice");

        chatController.sendMessage(spoofedDto, authedAccessor);

        ArgumentCaptor<MessageDTO> captor = ArgumentCaptor.forClass(MessageDTO.class);
        verify(chatService, times(2)).saveMessage(captor.capture());
        assertEquals("alice", captor.getAllValues().get(0).getSender());
        assertEquals("hello world", captor.getAllValues().get(0).getContent());
        verify(messagingTemplate, times(2)).convertAndSend(eq("/topic/public"), any(MessageDTO.class));
    }

    @Test
    void addUserRejectsReservedAndInvalidUsernames() {
        SimpMessageHeaderAccessor accessor = SimpMessageHeaderAccessor.create();
        accessor.setSessionAttributes(new HashMap<>());

        assertThrows(IllegalArgumentException.class,
                () -> chatController.addUser(new MessageDTO("ChatBot", "", "JOIN"), accessor));
        assertThrows(IllegalArgumentException.class,
                () -> chatController.addUser(new MessageDTO("a", "", "JOIN"), accessor));
        verify(userService, never()).connectUser(any());
    }

    @Test
    void clearHistoryFailsClosedWithoutTokenAndRequiresExactAdminToken() {
        ResponseEntity<Void> unconfigured = chatController.clearHistory("any-token");
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, unconfigured.getStatusCode());

        chatController.setAdminToken("strong-secret-token");
        assertEquals(HttpStatus.UNAUTHORIZED, chatController.clearHistory(null).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, chatController.clearHistory("wrong-token").getStatusCode());
        verify(chatService, never()).clearHistory();

        ResponseEntity<Void> authorized = chatController.clearHistory("strong-secret-token");
        assertEquals(HttpStatus.NO_CONTENT, authorized.getStatusCode());
        verify(chatService).clearHistory();
    }

    @Test
    void globalExceptionHandlerMasksInternalServerDetails() {
        ResponseEntity<Map<String, Object>> response =
                exceptionHandler.handleGeneral(new RuntimeException("jdbc:h2:mem:chatdb internal leak"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("An unexpected internal error occurred", response.getBody().get("message"));
        assertFalse(response.getBody().get("message").toString().contains("jdbc:h2"));
    }
}
