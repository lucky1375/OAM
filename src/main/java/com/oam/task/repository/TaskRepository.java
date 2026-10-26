package com.oam.task.repository;

import com.oam.task.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByAssigneeUserIdOrderByCreatedAtDesc(Long assigneeUserId);

    List<Task> findByCreatorUserIdOrderByCreatedAtDesc(Long creatorUserId);

    List<Task> findAllByOrderByCreatedAtDesc();

    List<Task> findByAssigneeUserId(Long assigneeUserId);
}
