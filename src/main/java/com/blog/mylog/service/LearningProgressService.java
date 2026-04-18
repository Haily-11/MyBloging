package com.blog.mylog.service;

import com.blog.mylog.entity.LearningProgress;
import com.blog.mylog.repository.LearningProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LearningProgressService {
    private final LearningProgressRepository learningProgressRepository;

    public List<LearningProgress> findAll() {
        return learningProgressRepository.findAll();
    }

    public LearningProgress save(LearningProgress progress) {
        return learningProgressRepository.save(progress);
    }

    public void deleteById(Long id) {
        learningProgressRepository.deleteById(id);
    }

    public LearningProgress findById(Long id) {
        return learningProgressRepository.findById(id).orElse(null);
    }
}
