package com.blog.mylog.config;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.GeneratePresignedUrlRequest;
import com.aliyun.oss.model.OSSObject;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Component
@Data
public class AliOssUtil {
    private static final Logger log = LoggerFactory.getLogger(AliOssUtil.class);

    @Value("${aliyun.oss.endpoint}")
    private String endpoint;

    @Value("${aliyun.oss.access-key-id}")
    private String accessKeyId;

    @Value("${aliyun.oss.access-key-secret}")
    private String accessKeySecret;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.url-prefix}")
    private String urlPrefix;

    @PostConstruct
    public void init() {
        System.out.println("========== OSS 配置检查 ==========");
        System.out.println("Endpoint: " + (endpoint != null && !endpoint.isEmpty() ? endpoint : "❌ 未配置"));
        System.out.println("AccessKeyId: " + (accessKeyId != null && !accessKeyId.isEmpty() ? "✅ 已配置 (" + accessKeyId.substring(0, 8) + "...)" : "❌ 未配置"));
        System.out.println("AccessKeySecret: " + (accessKeySecret != null && !accessKeySecret.isEmpty() ? "✅ 已配置" : "❌ 未配置"));
        System.out.println("BucketName: " + (bucketName != null && !bucketName.isEmpty() ? bucketName : "❌ 未配置"));
        System.out.println("URL Prefix: " + (urlPrefix != null && !urlPrefix.isEmpty() ? urlPrefix : "❌ 未配置"));
        System.out.println("==================================");
        
        if (endpoint == null || endpoint.isEmpty() || 
            accessKeyId == null || accessKeyId.isEmpty() ||
            accessKeySecret == null || accessKeySecret.isEmpty() ||
            bucketName == null || bucketName.isEmpty()) {
            System.err.println("⚠️  警告：OSS 配置不完整，文章详情功能可能无法使用！");
        }
    }

    public String upload(MultipartFile file) throws IOException {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String fileName = UUID.randomUUID().toString() + extension;

            ossClient.putObject(bucketName, fileName, file.getInputStream());

            return urlPrefix + fileName;
        } finally {
            ossClient.shutdown();
        }
    }

    public String uploadArticleContent(String content, Long articleId) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            String fileName = "articles/" + articleId + "/content.md";
            byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
            
            ossClient.putObject(bucketName, fileName, new ByteArrayInputStream(contentBytes));

            return fileName;
        } finally {
            ossClient.shutdown();
        }
    }

    public String generatePresignedUrl(String objectKey, int expireMinutes) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            Date expiration = new Date(System.currentTimeMillis() + expireMinutes * 60 * 1000L);
            
            GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucketName, objectKey);
            request.setExpiration(expiration);
            
            URL presignedUrl = ossClient.generatePresignedUrl(request);
            return presignedUrl.toString();
        } finally {
            ossClient.shutdown();
        }
    }

    public String getArticleContent(Long articleId) {
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            String objectKey = "articles/" + articleId + "/content.md";
            OSSObject ossObject = ossClient.getObject(bucketName, objectKey);
            
            try (InputStream is = ossObject.getObjectContent();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                return content.toString();
            }
        } catch (Exception e) {
            log.error("读取OSS文章失败: {}", articleId, e);
            return null;
        } finally {
            ossClient.shutdown();
        }
    }
}
