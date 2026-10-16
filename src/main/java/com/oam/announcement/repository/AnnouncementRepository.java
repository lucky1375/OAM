package com.oam.announcement.repository;

import com.oam.announcement.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    List<Announcement> findByStatusOrderByTopDescPublishTimeDesc(String status);

    List<Announcement> findByStatusAndTitleContainingOrderByTopDescPublishTimeDesc(String status, String keyword);

    List<Announcement> findByTitleContainingOrderByTopDescPublishTimeDesc(String keyword);
}
