package com.oam.forum.repository;

import com.oam.forum.entity.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {

    List<ForumPost> findBySectionIdOrderByCreatedAtDesc(Long sectionId);

    List<ForumPost> findByTitleContainingOrContentContainingOrderByCreatedAtDesc(String title, String content);

    long countBySectionId(Long sectionId);
}
