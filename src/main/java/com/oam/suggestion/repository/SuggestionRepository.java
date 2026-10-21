package com.oam.suggestion.repository;

import com.oam.suggestion.entity.Suggestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SuggestionRepository extends JpaRepository<Suggestion, Long> {

    List<Suggestion> findAllByOrderByCreatedAtDesc();

    List<Suggestion> findBySubmitterUserIdOrderByCreatedAtDesc(Long submitterUserId);

    long countByStatus(String status);

    long countByCategory(String category);
}
