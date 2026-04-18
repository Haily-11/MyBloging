package com.blog.mylog.service;


import com.blog.mylog.entity.Article;
import org.springframework.data.domain.Page;

public interface ArticleService {
    Article save(Article article);
    Article findById(Long id);
    Page<Article> findAll(int page, int size);
    Page<Article> findByUser(Long userId, int page, int size);
    void deleteById(Long id);
    void incrementViewCount(Long id);
}