package com.blog.mylog.service.impl;

import com.blog.mylog.entity.Article;
import com.blog.mylog.repository.ArticleRepository;
import com.blog.mylog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;

    @Override
    public Article save(Article article) {
        String content = article.getContent();
        if (content != null) {
            String cleanContent = content.replaceAll("<[^>]*>", "");
            article.setWordCount(cleanContent.length());
            
            int minutes = (int) Math.ceil(article.getWordCount() / 300.0);
            article.setReadTime(Math.max(1, minutes));
        } else {
            article.setWordCount(0);
            article.setReadTime(1);
        }
        
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
    public Page<Article> findByMonth(String yearMonth, int page, int size) {
        // 解析年-月
        int year = Integer.parseInt(yearMonth.split("-")[0]);
        int month = Integer.parseInt(yearMonth.split("-")[1]);
        
        // 这里使用简单的内存过滤（若数据量大可改为数据库查询）
        List<Article> all = articleRepository.findAll();
        List<Article> filtered = all.stream()
                .filter(a -> a.getCreateTime().getYear() == year && a.getCreateTime().getMonthValue() == month)
                .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
                .collect(Collectors.toList());
                
        int start = (page - 1) * size;
        int end = Math.min(start + size, filtered.size());
        
        Page<Article> paged = new PageImpl<>(filtered.subList(start, end), PageRequest.of(page - 1, size), filtered.size());
        return paged;
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