package com.blog.mylog.service;

import com.blog.mylog.entity.Tech;
import com.blog.mylog.repository.TechRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TechService {

    private final TechRepository techRepository;

    public List<Tech> findAll() {
        return techRepository.findAll();
    }

    public Tech save(Tech tech) {
        return techRepository.save(tech);
    }

    public void deleteById(Long id) {
        techRepository.deleteById(id);
    }

    public Tech findById(Long id) {
        return techRepository.findById(id).orElse(null);
    }
}
