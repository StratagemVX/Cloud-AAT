package com.chatapp.service;

import com.chatapp.dto.MessageDTO;
import com.chatapp.model.ChatMessage;
import com.chatapp.model.ChatMessage.MessageType;
import com.chatapp.repository.ChatMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatMessageRepository messageRepository;

    @InjectMocks
    private ChatService chatService;

    private ChatMessage testMessage;
    private MessageDTO testMessageDTO;

    @BeforeEach
    void setUp() {
        testMessage = new ChatMessage("sender1", "Hello World", MessageType.CHAT);
        testMessage.setId(1L);
        testMessage.setTimestamp(LocalDateTime.of(2026, 3, 7, 14, 30));

        testMessageDTO = new MessageDTO("sender1", "Hello World", "CHAT", null);
    }

    @Test
    void saveMessage_ShouldPersistAndReturnWithTimestamp() {
        // Given
        when(messageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage msg = invocation.getArgument(0);
            msg.setId(1L);
            msg.setTimestamp(LocalDateTime.of(2026, 3, 7, 14, 30));
            return msg;
        });

        // When
        MessageDTO result = chatService.saveMessage(testMessageDTO);

        // Then
        assertNotNull(result);
        assertEquals("sender1", result.getSender());
        assertEquals("Hello World", result.getContent());
        assertEquals("CHAT", result.getType());
        assertEquals("14:30", result.getTimestamp());
        verify(messageRepository).save(any(ChatMessage.class));
    }

    @Test
    void saveMessage_JoinType_ShouldHandleCorrectly() {
        // Given
        MessageDTO joinDTO = new MessageDTO("user1", "user1 joined", "JOIN", null);
        when(messageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage msg = invocation.getArgument(0);
            msg.setId(2L);
            msg.setTimestamp(LocalDateTime.of(2026, 3, 7, 15, 0));
            return msg;
        });

        // When
        MessageDTO result = chatService.saveMessage(joinDTO);

        // Then
        assertNotNull(result);
        assertEquals("JOIN", result.getType());
        assertEquals("15:00", result.getTimestamp());
        verify(messageRepository).save(any(ChatMessage.class));
    }

    @Test
    void getRecentMessages_ShouldReturnFormattedMessages() {
        // Given
        ChatMessage msg1 = new ChatMessage("user1", "Message 1", MessageType.CHAT);
        msg1.setTimestamp(LocalDateTime.of(2026, 3, 7, 10, 15));

        ChatMessage msg2 = new ChatMessage("user2", "Message 2", MessageType.CHAT);
        msg2.setTimestamp(LocalDateTime.of(2026, 3, 7, 10, 20));

        when(messageRepository.findTop50ByOrderByTimestampAsc())
                .thenReturn(Arrays.asList(msg1, msg2));

        // When
        List<MessageDTO> result = chatService.getRecentMessages();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("user1", result.get(0).getSender());
        assertEquals("Message 1", result.get(0).getContent());
        assertEquals("10:15", result.get(0).getTimestamp());
        assertEquals("user2", result.get(1).getSender());
        assertEquals("10:20", result.get(1).getTimestamp());
        verify(messageRepository).findTop50ByOrderByTimestampAsc();
    }

    @Test
    void getRecentMessages_EmptyHistory_ShouldReturnEmptyList() {
        // Given
        when(messageRepository.findTop50ByOrderByTimestampAsc()).thenReturn(Arrays.asList());

        // When
        List<MessageDTO> result = chatService.getRecentMessages();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(messageRepository).findTop50ByOrderByTimestampAsc();
    }

    @Test
    void clearHistory_ShouldDeleteAllMessages() {
        // Given
        doNothing().when(messageRepository).deleteAll();

        // When
        chatService.clearHistory();

        // Then
        verify(messageRepository).deleteAll();
    }

    @Test
    void saveMessage_LeaveType_ShouldHandleCorrectly() {
        // Given
        MessageDTO leaveDTO = new MessageDTO("user1", "user1 left", "LEAVE", null);
        when(messageRepository.save(any(ChatMessage.class))).thenAnswer(invocation -> {
            ChatMessage msg = invocation.getArgument(0);
            msg.setId(3L);
            msg.setTimestamp(LocalDateTime.of(2026, 3, 7, 16, 45));
            return msg;
        });

        // When
        MessageDTO result = chatService.saveMessage(leaveDTO);

        // Then
        assertNotNull(result);
        assertEquals("LEAVE", result.getType());
        assertEquals("16:45", result.getTimestamp());
        verify(messageRepository).save(any(ChatMessage.class));
    }
}
