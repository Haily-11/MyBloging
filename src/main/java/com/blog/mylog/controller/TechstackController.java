package com.blog.mylog.controller;

import com.blog.mylog.entity.Article;
import com.blog.mylog.entity.CheckIn;
import com.blog.mylog.entity.LearningProgress;
import com.blog.mylog.entity.Tech;
import com.blog.mylog.entity.User;
import com.blog.mylog.service.ArticleService;
import com.blog.mylog.service.CheckInService;
import com.blog.mylog.service.LearningProgressService;
import com.blog.mylog.service.TechService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class TechstackController {
    
    private final TechService techService;
    private final ArticleService articleService;
    private final LearningProgressService learningProgressService;
    private final CheckInService checkInService;
    
    @GetMapping("/techstack")
    public String techstack(Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        model.addAttribute("user", user);
        
        List<Tech> techs = techService.findAll();
        model.addAttribute("techs", techs);
        
        List<LearningProgress> progresses = learningProgressService.findAll();
        model.addAttribute("progresses", progresses);
        
        List<CheckIn> recentCheckIns = checkInService.getRecentCheckIns();
        model.addAttribute("recentCheckIns", recentCheckIns);
        
        CheckIn todayCheckIn = checkInService.getTodayCheckIn();
        model.addAttribute("todayCheckIn", todayCheckIn);
        
        Map<Long, List<Article>> techArticles = new HashMap<>();
        List<Article> allArticles = articleService.findAll(1, 1000).getContent();
        
        for (Tech tech : techs) {
            List<Article> matchedArticles = new ArrayList<>();
            String techName = tech.getName().toLowerCase();
            
            for (Article article : allArticles) {
                String content = (article.getTitle() + " " + article.getContent()).toLowerCase();
                if (content.contains(techName)) {
                    matchedArticles.add(article);
                }
            }
            techArticles.put(tech.getId(), matchedArticles);
        }
        
        model.addAttribute("techArticles", techArticles);
        return "techstack";
    }
    
    @PostMapping("/tech/add")
    public String addTech(@RequestParam String name,
                          @RequestParam(required = false) String color,
                          @RequestParam(required = false) String description,
                          HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        Tech tech = new Tech();
        tech.setName(name);
        tech.setColor(color != null && !color.isEmpty() ? color : "#00f0ff");
        tech.setDescription(description);
        techService.save(tech);
        return "redirect:/techstack";
    }
    
    @GetMapping("/tech/edit/{id}")
    public String editTech(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        Tech tech = techService.findById(id);
        model.addAttribute("tech", tech);
        model.addAttribute("isEdit", true);
        return "techstack";
    }
    
    @PostMapping("/tech/update")
    public String updateTech(@RequestParam Long id,
                             @RequestParam String name,
                             @RequestParam(required = false) String color,
                             @RequestParam(required = false) String description,
                             HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        Tech tech = techService.findById(id);
        tech.setName(name);
        tech.setColor(color != null && !color.isEmpty() ? color : "#00f0ff");
        tech.setDescription(description);
        techService.save(tech);
        return "redirect:/techstack";
    }
    
    @GetMapping("/tech/delete/{id}")
    public String deleteTech(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        techService.deleteById(id);
        return "redirect:/techstack";
    }
    
    @PostMapping("/progress/add")
    public String addProgress(@RequestParam String name,
                              @RequestParam(required = false) String color,
                              HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        LearningProgress progress = new LearningProgress();
        progress.setName(name);
        progress.setColor(color != null && !color.isEmpty() ? color : "#00f0ff");
        progress.setProgress(0);
        learningProgressService.save(progress);
        return "redirect:/techstack";
    }
    
    @PostMapping("/progress/increment/{id}")
    public String incrementProgress(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        LearningProgress progress = learningProgressService.findById(id);
        if (progress != null) {
            int newProgress = progress.getProgress() + 10;
            if (newProgress > 100) {
                learningProgressService.deleteById(id);
            } else {
                progress.setProgress(newProgress);
                learningProgressService.save(progress);
            }
        }
        return "redirect:/techstack";
    }
    
    @GetMapping("/progress/delete/{id}")
    public String deleteProgress(@PathVariable Long id, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        learningProgressService.deleteById(id);
        return "redirect:/techstack";
    }
    
    @PostMapping("/checkin")
    public String doCheckIn(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        checkInService.checkIn();
        return "redirect:/techstack";
    }
}
