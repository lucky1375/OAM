package com.oam.task.repository;

import com.oam.task.entity.TaskProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskProgressRepository extends JpaRepository<TaskProgress, Long> {

    List<TaskProgress> findByTaskIdOrderByCreatedAtDesc(Long taskId);

    void deleteByTaskId(Long taskId);
}
