package com.oam.forum.repository;

import com.oam.forum.entity.ForumLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ForumLikeRepository extends JpaRepository<ForumLike, Long> {

    Optional<ForumLike> findByPostIdAndUserId(Long postId, Long userId);

    boolean existsByPostIdAndUserId(Long postId, Long userId);

    long countByPostId(Long postId);

    void deleteByPostId(Long postId);
}
