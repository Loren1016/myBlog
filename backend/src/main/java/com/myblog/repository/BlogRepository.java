package com.myblog.repository;

import com.myblog.entity.Blog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<Blog, String> {
    Optional<Blog> findBySlug(String slug);
    Page<Blog> findByStatus(String status, Pageable pageable);
}
