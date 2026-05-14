package com.myblog.repository;

import com.myblog.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project, String> {
    Optional<Project> findBySlug(String slug);
    List<Project> findTop5ByOrderByUpdatedAtDesc();
}
