package com.blog.mylog.repository;

import com.blog.mylog.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    @Query(value = "SELECT DATE(create_time) as date, COUNT(*) as count " +
           "FROM article " +
           "WHERE create_time >= :startDate AND create_time <= :endDate " +
           "GROUP BY DATE(create_time) " +
           "ORDER BY date ASC", nativeQuery = true)
    List<Object[]> countArticlesByDateRange(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    Page<Article> findByStatusOrderByCreateTimeDesc(Integer status, Pageable pageable);

    Page<Article> findByUserIdOrderByCreateTimeDesc(Long userId, Pageable pageable);
}