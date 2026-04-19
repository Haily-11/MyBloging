package com.blog.mylog.service;

import com.blog.mylog.entity.Project;
import java.util.List;

public interface ProjectService {
    List<Project> findAll();
    Project save(Project project);
    void deleteById(Long id);
}