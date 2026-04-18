package com.blog.mylog.repository;

import com.blog.mylog.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByArticleIdAndStatusOrderByCreateTimeAsc(Long articleId, Integer status);
}