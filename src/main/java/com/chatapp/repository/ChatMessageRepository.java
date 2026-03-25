package com.chatapp.repository;

import com.chatapp.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link ChatMessage} entities.
 */
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    /** Return the most recent N messages ordered by timestamp ascending. */
    List<ChatMessage> findTop50ByOrderByTimestampAsc();
}
