package com.oam.forum.repository;

import com.oam.forum.entity.ForumSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForumSectionRepository extends JpaRepository<ForumSection, Long> {

    List<ForumSection> findAllByOrderBySortAscIdAsc();

    boolean existsByName(String name);

    boolean existsByNameAndIdNot(String name, Long id);
}
