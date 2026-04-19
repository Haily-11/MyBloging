package com.blog.mylog.repository;

import com.blog.mylog.entity.Note;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NoteRepository extends JpaRepository<Note, Long> {
    List<Note> findByUserIdOrderByCreateTimeDesc(Long userId);
}
