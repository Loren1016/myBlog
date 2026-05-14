package com.myblog.service;

import com.myblog.entity.Blog;
import com.myblog.exception.ResourceNotFoundException;
import com.myblog.repository.BlogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class BlogService {

    private final BlogRepository blogRepository;

    public BlogService(BlogRepository blogRepository) {
        this.blogRepository = blogRepository;
    }

    /* ---- Public ---- */

    public Page<Blog> findPublished(int page, int size) {
        return blogRepository.findByStatus("published",
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "publishedAt")));
    }

    public Blog findBySlugPublished(String slug) {
        Blog blog = blogRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", "slug", slug));
        if (!"published".equals(blog.getStatus())) {
            throw new ResourceNotFoundException("Blog", "slug", slug);
        }
        return blog;
    }

    /* ---- Admin ---- */

    public Page<Blog> findAllPaginated(int page, int size) {
        return blogRepository.findAll(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "updatedAt")));
    }

    public Blog findById(String id) {
        return blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", "id", id));
    }

    public Blog create(Blog blog) {
        if ("published".equals(blog.getStatus())) {
            blog.setPublishedAt(LocalDateTime.now());
        }
        return blogRepository.save(blog);
    }

    public Blog update(String id, Blog updated) {
        Blog existing = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", "id", id));
        existing.setTitle(updated.getTitle());
        existing.setSlug(updated.getSlug());
        existing.setSummary(updated.getSummary());
        existing.setContent(updated.getContent());
        existing.setCoverImage(updated.getCoverImage());
        existing.setTags(updated.getTags());
        existing.setCategory(updated.getCategory());
        existing.setStatus(updated.getStatus());
        if ("published".equals(updated.getStatus()) && existing.getPublishedAt() == null) {
            existing.setPublishedAt(LocalDateTime.now());
        }
        return blogRepository.save(existing);
    }

    public void delete(String id) {
        Blog blog = blogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blog", "id", id));
        blogRepository.delete(blog);
    }
}
