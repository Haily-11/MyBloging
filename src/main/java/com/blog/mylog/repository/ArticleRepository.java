package com.blog.mylog.repository;

import com.blog.mylog.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    Page<Article> findByStatusOrderByCreateTimeDesc(Integer status, Pageable pageable);
    Page<Article> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);
}