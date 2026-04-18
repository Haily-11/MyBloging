package com.blog.mylog.controller;

import com.blog.mylog.entity.Article;
import com.blog.mylog.entity.User;
import com.blog.mylog.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpSession;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/")
    public String index(Model model) {
        Page<Article> articlePage = articleService.findAll(1, 5);
        model.addAttribute("articles", articlePage.getContent());

        Page<Article> recentPage = articleService.findAll(1, 5);
        model.addAttribute("recentArticles", recentPage.getContent());

        // 统计信息
        List<Article> allArticles = articleService.findAll(1, 10000).getContent();
        model.addAttribute("totalArticles", allArticles.size());
        
        int totalViews = allArticles.stream().mapToInt(a -> a.getViewCount() != null ? a.getViewCount() : 0).sum();
        model.addAttribute("totalViews", totalViews);
        
        int totalLikes = allArticles.stream().mapToInt(a -> a.getLikeCount() != null ? a.getLikeCount() : 0).sum();
        model.addAttribute("totalLikes", totalLikes);

        // 分类列表
        List<String> categories = allArticles.stream()
                .map(Article::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("categories", categories);

        return "index";
    }

    @GetMapping("/article/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Article article = articleService.findById(id);
        articleService.incrementViewCount(id);
        model.addAttribute("article", article);
        return "detail";
    }

    @PostMapping("/article/like/{id}")
    public String likeArticle(@PathVariable Long id) {
        Article article = articleService.findById(id);
        article.setLikeCount(article.getLikeCount() + 1);
        articleService.save(article);
        return "redirect:/article/" + id;
    }

    @GetMapping("/articles")
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       Model model) {
        Page<Article> articlePage = articleService.findAll(page, size);
        model.addAttribute("articles", articlePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", articlePage.getTotalPages());
        model.addAttribute("recentArticles", articleService.findAll(1, 5).getContent());
        return "list";
    }

    @PostMapping("/doWrite")
    public String doWrite(@RequestParam String title,
                          @RequestParam String content,
                          @RequestParam(required = false) String summary,
                          @RequestParam(required = false) String category,
                          @RequestParam(required = false) String tags,
                          HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Article article = new Article();
        article.setTitle(title);
        article.setContent(content);
        article.setSummary(summary != null && !summary.isEmpty() ? summary :
                (content.length() > 150 ? content.substring(0, 150) + "..." : content));
        article.setCategory(category);
        article.setTags(tags);
        article.setUserId(user.getId());
        article.setStatus(2);

        articleService.save(article);
        return "redirect:/";
    }

    @GetMapping("/write")
    public String write(Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        List<Article> allArticles = articleService.findAll(1, 10000).getContent();
        List<String> categories = allArticles.stream()
                .map(Article::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("categories", categories);
        
        List<String> allTags = allArticles.stream()
                .map(Article::getTags)
                .filter(t -> t != null && !t.isEmpty())
                .flatMap(t -> java.util.Arrays.stream(t.split(",")))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("tags", allTags);
        
        return "write";
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return "redirect:/login";
        }
        
        Article article = articleService.findById(id);
        model.addAttribute("article", article);
        model.addAttribute("isEdit", true);
        
        List<Article> allArticles = articleService.findAll(1, 10000).getContent();
        List<String> categories = allArticles.stream()
                .map(Article::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("categories", categories);
        
        List<String> allTags = allArticles.stream()
                .map(Article::getTags)
                .filter(t -> t != null && !t.isEmpty())
                .flatMap(t -> java.util.Arrays.stream(t.split(",")))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .distinct()
                .sorted()
                .collect(java.util.stream.Collectors.toList());
        model.addAttribute("tags", allTags);
        
        return "write";
    }

    @PostMapping("/doEdit")
    public String doEdit(@RequestParam Long id,
                         @RequestParam String title,
                         @RequestParam String content,
                         @RequestParam(required = false) String summary,
                         HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        Article article = articleService.findById(id);
        article.setTitle(title);
        article.setContent(content);
        article.setSummary(summary != null && !summary.isEmpty() ? summary :
                (content.length() > 150 ? content.substring(0, 150) + "..." : content));

        articleService.save(article);
        return "redirect:/article/" + id;
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        articleService.deleteById(id);
        return "redirect:/";
    }
}