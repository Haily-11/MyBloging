package com.blog.mylog.service.impl;

import com.blog.mylog.entity.Article;
import com.blog.mylog.repository.ArticleRepository;
import com.blog.mylog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;

    @Override
    public Article save(Article article) {
        return articleRepository.save(article);
    }

    @Override
    public Article findById(Long id) {
        return articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("文章不存在"));
    }

    @Override
    public Page<Article> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size,
                Sort.by(Sort.Direction.DESC, "createTime"));
        return articleRepository.findByStatusOrderByCreateTimeDesc(2, pageable);
    }

    @Override
    public Page<Article> findByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size,
                Sort.by(Sort.Direction.DESC, "createTime"));
        return articleRepository.findByUserIdOrderByCreateTimeDesc(userId, pageable);
    }

    @Override
    public void deleteById(Long id) {
        articleRepository.deleteById(id);
    }

    @Override
    public void incrementViewCount(Long id) {
        Article article = findById(id);
        article.setViewCount(article.getViewCount() + 1);
        articleRepository.save(article);
    }
}