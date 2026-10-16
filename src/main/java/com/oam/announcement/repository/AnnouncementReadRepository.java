package com.oam.announcement.repository;

import com.oam.announcement.entity.AnnouncementRead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnnouncementReadRepository extends JpaRepository<AnnouncementRead, Long> {

    Optional<AnnouncementRead> findByAnnouncementIdAndUserId(Long announcementId, Long userId);

    boolean existsByAnnouncementIdAndUserId(Long announcementId, Long userId);

    long countByAnnouncementId(Long announcementId);

    List<AnnouncementRead> findByAnnouncementIdOrderByCreatedAtDesc(Long announcementId);

    void deleteByAnnouncementId(Long announcementId);
}
