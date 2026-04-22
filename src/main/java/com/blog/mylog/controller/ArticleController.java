package com.blog.mylog.controller;

import com.blog.mylog.config.AliOssUtil;
import com.blog.mylog.entity.Article;
import com.blog.mylog.entity.User;
import com.blog.mylog.service.ArticleService;
import com.blog.mylog.util.CsrfTokenUtil;
import com.blog.mylog.util.XssFilter;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class ArticleController {
    private static final Logger log = LoggerFactory.getLogger(ArticleController.class);

    private final ArticleService articleService;
    private final AliOssUtil aliOssUtil;

    @GetMapping("/")
    public String index(Model model, @RequestParam(defaultValue = "1") int page, HttpSession session) {
        Page<Article> articlePage = articleService.findAll(page, 10);
        model.addAttribute("articles", articlePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", articlePage.getTotalPages());

        List<Article> allArticles = articleService.findAll(1, 10000).getContent();
        Map<String, Long> tagCounts = allArticles.stream()
                .filter(a -> a.getTags() != null)
                .flatMap(a -> Arrays.stream(a.getTags().split(",")))
                .map(String::trim)
                .filter(t -> !t.isEmpty())
                .collect(Collectors.groupingBy(t -> t, Collectors.counting()));
        model.addAttribute("tagCloud", tagCounts);

        Map<String, Long> monthArchives = allArticles.stream()
                .filter(a -> a.getCreateTime() != null)
                .collect(Collectors.groupingBy(
                        a -> a.getCreateTime().getYear() + "-" + String.format("%02d", a.getCreateTime().getMonthValue()), 
                        Collectors.counting()
                ));
        LinkedHashMap<String, Long> sortedArchives = monthArchives.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByKey().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        
        model.addAttribute("monthArchives", sortedArchives);

        List<Article> recentArticles = allArticles.stream()
                .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
                .limit(5)
                .collect(Collectors.toList());
        model.addAttribute("recentArticles", recentArticles);

        model.addAttribute("totalArticles", allArticles.size());
        model.addAttribute("totalViews", allArticles.stream().mapToInt(a -> a.getViewCount() != null ? a.getViewCount() : 0).sum());
        model.addAttribute("totalLikes", allArticles.stream().mapToInt(a -> a.getLikeCount() != null ? a.getLikeCount() : 0).sum());

        List<String> categories = allArticles.stream()
                .map(Article::getCategory)
                .filter(c -> c != null && !c.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        model.addAttribute("categories", categories);

        if (session.getAttribute("user") != null) {
            model.addAttribute("csrfToken", CsrfTokenUtil.generateToken(session));
        }

        return "index";
    }

    @GetMapping("/article/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Article article = articleService.findById(id);
        articleService.incrementViewCount(id);
        
        String content = aliOssUtil.getArticleContent(id);
        if (content == null || content.isEmpty()) {
            content = article.getContent() != null ? article.getContent() : "文章内容加载失败";
        }
        
        model.addAttribute("article", article);
        model.addAttribute("content", content);
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
                       @RequestParam(required = false) String month,
                       Model model) {
        Page<Article> articlePage;
        if (month != null) {
            articlePage = articleService.findByMonth(month, page, 10);
            model.addAttribute("filterLabel", "归档：" + month);
        } else {
            articlePage = articleService.findAll(page, 10);
            model.addAttribute("filterLabel", "全部文章");
        }
        
        model.addAttribute("articles", articlePage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", articlePage.getTotalPages());
        return "list";
    }

    private String cleanMarkdown(String content) {
        if (content == null) return "";
        return content
                .replaceAll("#+\\s*", "")
                .replaceAll("\\*\\*(.+?)\\*\\*", "$1")
                .replaceAll("__(.+?)__", "$1")
                .replaceAll("\\*(.+?)\\*", "$1")
                .replaceAll("_(.+?)_", "$1")
                .replaceAll("!\\[(.*?)\\]\\(.*?\\)", "$1")
                .replaceAll("\\[(.*?)\\]\\(.*?\\)", "$1")
                .replaceAll("`{1,3}[^`]*`{1,3}", "")
                .replaceAll("\\[\\s*\\]\\[.*?\\]", "")
                .replaceAll("^>\\s*", "")
                .replaceAll("^[-*+]\\s+", "")
                .replaceAll("^\\d+\\.\\s+", "")
                .replaceAll("https?://\\S+", "")
                .replaceAll("<[^>]*>", "")
                .replaceAll("\\s+", " ")
                .trim();
    }

    @PostMapping("/doWrite")
    public String doWrite(@RequestParam String title,
                          @RequestParam String content,
                          @RequestParam(required = false) String summary,
                          @RequestParam(required = false) String category,
                          @RequestParam(required = false) String tags,
                          @RequestParam(required = false) String csrfToken,
                          HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if (!CsrfTokenUtil.validateToken(session, csrfToken)) {
            return "redirect:/write?error=CSRF验证失败";
        }

        Article article = new Article();
        article.setTitle(XssFilter.clean(title));
        
        if (summary != null && !summary.isEmpty()) {
            article.setSummary(XssFilter.clean(summary));
        } else {
            String plainText = cleanMarkdown(content);
            article.setSummary(plainText.length() > 150 ? plainText.substring(0, 150) + "..." : plainText);
        }
        
        article.setCategory(XssFilter.clean(category));
        article.setTags(XssFilter.clean(tags));
        article.setUserId(user.getId());
        article.setStatus(2);

        try {
            Article savedArticle = articleService.save(article);
            String objectKey = aliOssUtil.uploadArticleContent(content, savedArticle.getId());
            savedArticle.setContentUrl(objectKey);
            articleService.save(savedArticle);
        } catch (Exception e) {
            log.error("文章发布异常", e);
        }
        
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
        model.addAttribute("csrfToken", CsrfTokenUtil.generateToken(session));
        
        String content = aliOssUtil.getArticleContent(id);
        if (content == null || content.isEmpty()) {
            content = article.getContent() != null ? article.getContent() : "";
        }
        model.addAttribute("content", content);
        
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
                         @RequestParam(required = false) String csrfToken,
                         HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/login";
        }

        if (!CsrfTokenUtil.validateToken(session, csrfToken)) {
            return "redirect:/edit/" + id + "?error=CSRF验证失败";
        }

        Article article = articleService.findById(id);
        article.setTitle(XssFilter.clean(title));

        if (summary != null && !summary.isEmpty()) {
            article.setSummary(XssFilter.clean(summary));
        } else {
            String plainText = cleanMarkdown(content);
            article.setSummary(plainText.length() > 150 ? plainText.substring(0, 150) + "..." : plainText);
        }

        String contentUrl = aliOssUtil.uploadArticleContent(content, id);
        if (contentUrl != null && !contentUrl.isEmpty()) {
            article.setContentUrl(contentUrl);
        } else {
            article.setContent(content);
        }

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