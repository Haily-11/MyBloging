package com.blog.mylog.service;

import com.blog.mylog.entity.Note;
import com.blog.mylog.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteService {
    private final NoteRepository noteRepository;

    public List<Note> findByUserId(Long userId) {
        return noteRepository.findByUserIdOrderByCreateTimeDesc(userId);
    }

    public Note save(Note note) {
        return noteRepository.save(note);
    }

    public void deleteById(Long id) {
        noteRepository.deleteById(id);
    }
}
