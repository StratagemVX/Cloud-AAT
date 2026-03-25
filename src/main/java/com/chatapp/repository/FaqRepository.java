package com.chatapp.repository;

import com.chatapp.model.FaqEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link FaqEntry} entities.
 */
@Repository
public interface FaqRepository extends JpaRepository<FaqEntry, Long> {
}
