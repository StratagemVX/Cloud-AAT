package com.chatapp.service;

import com.chatapp.dto.MessageDTO;
import com.chatapp.model.ChatMessage;
import com.chatapp.model.ChatMessage.MessageType;
import com.chatapp.repository.ChatMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for persisting chat messages and retrieving message history.
 */
@Service
public class ChatService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("HH:mm");

    private final ChatMessageRepository messageRepository;

    public ChatService(ChatMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    // ─── Persistence ───────────────────────────────────────────────────

    /**
     * Save a chat message to the database.
     *
     * @param dto the incoming message DTO
     * @return the persisted message as a DTO (now with a timestamp)
     */
    @Transactional
    public MessageDTO saveMessage(MessageDTO dto) {
        ChatMessage entity = new ChatMessage(
                dto.getSender(),
                dto.getContent(),
                MessageType.valueOf(dto.getType()));
        entity = messageRepository.save(entity);

        dto.setTimestamp(entity.getTimestamp().format(FMT));
        return dto;
    }

    // ─── History ───────────────────────────────────────────────────────

    /**
     * Retrieve the 50 most recent messages for chat-history display.
     */
    public List<MessageDTO> getRecentMessages() {
        return messageRepository.findTop50ByOrderByTimestampAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    /**
     * Delete all chat messages (clear history).
     */
    @Transactional
    public void clearHistory() {
        messageRepository.deleteAll();
    }

    // ─── Helpers ───────────────────────────────────────────────────────

    private MessageDTO toDTO(ChatMessage msg) {
        return new MessageDTO(
                msg.getSender(),
                msg.getContent(),
                msg.getType().name(),
                msg.getTimestamp() != null ? msg.getTimestamp().format(FMT) : "");
    }
}
