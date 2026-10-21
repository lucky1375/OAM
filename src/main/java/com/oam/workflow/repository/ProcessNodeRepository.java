package com.oam.workflow.repository;

import com.oam.workflow.entity.ProcessNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProcessNodeRepository extends JpaRepository<ProcessNode, Long> {

    List<ProcessNode> findByTypeOrderByNodeOrderAsc(String type);

    void deleteByType(String type);
}
