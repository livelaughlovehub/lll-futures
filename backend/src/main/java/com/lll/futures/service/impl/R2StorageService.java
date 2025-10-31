package com.lll.futures.service.impl;

import com.lll.futures.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Cloudflare R2 storage implementation
 * Used for production environment
 */
@Service("r2StorageService")
public class R2StorageService implements StorageService {
    
    @Value("${storage.r2.endpoint}")
    private String r2Endpoint;
    
    @Value("${storage.r2.access-key}")
    private String r2AccessKey;
    
    @Value("${storage.r2.secret-key}")
    private String r2SecretKey;
    
    @Value("${storage.r2.bucket}")
    private String r2Bucket;
    
    @Value("${storage.r2.region:auto}")
    private String r2Region;
    
    private S3Client s3Client;
    
    private S3Client getS3Client() {
        if (s3Client == null) {
            AwsBasicCredentials credentials = AwsBasicCredentials.create(r2AccessKey, r2SecretKey);
            
            s3Client = S3Client.builder()
                .endpointOverride(java.net.URI.create(r2Endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .region(Region.of(r2Region))
                .build();
        }
        return s3Client;
    }
    
    @Override
    public String uploadFile(MultipartFile file, String folder) {
        return uploadFile(file, folder, null);
    }
    
    @Override
    public String uploadFile(MultipartFile file, String folder, String filename) {
        try {
            // Generate filename if not provided
            if (filename == null || filename.isEmpty()) {
                String originalFilename = file.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                filename = UUID.randomUUID().toString() + extension;
            }
            
            // Build S3 key
            String s3Key = filename;
            if (folder != null && !folder.isEmpty()) {
                s3Key = folder + "/" + filename;
            }
            
            // Upload to R2
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(r2Bucket)
                .key(s3Key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();
            
            getS3Client().putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
            
            // Return public URL
            return r2Endpoint + "/" + r2Bucket + "/" + s3Key;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to R2: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            // Extract S3 key from URL
            String s3Key = extractS3KeyFromUrl(fileUrl);
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(r2Bucket)
                .key(s3Key)
                .build();
            
            getS3Client().deleteObject(deleteObjectRequest);
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    @Override
    public InputStream getFileStream(String fileUrl) {
        try {
            String s3Key = extractS3KeyFromUrl(fileUrl);
            
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(r2Bucket)
                .key(s3Key)
                .build();
            
            return getS3Client().getObject(getObjectRequest);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to read file from R2: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<String> listFiles(String folder) {
        List<String> files = new ArrayList<>();
        try {
            ListObjectsV2Request listObjectsRequest = ListObjectsV2Request.builder()
                .bucket(r2Bucket)
                .prefix(folder != null ? folder + "/" : "")
                .build();
            
            ListObjectsV2Response response = getS3Client().listObjectsV2(listObjectsRequest);
            
            for (S3Object s3Object : response.contents()) {
                files.add(r2Endpoint + "/" + r2Bucket + "/" + s3Object.key());
            }
            
        } catch (Exception e) {
            // Return empty list on error
        }
        return files;
    }
    
    @Override
    public boolean fileExists(String fileUrl) {
        try {
            String s3Key = extractS3KeyFromUrl(fileUrl);
            
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(r2Bucket)
                .key(s3Key)
                .build();
            
            getS3Client().headObject(headObjectRequest);
            return true;
            
        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }
    
    private String extractS3KeyFromUrl(String fileUrl) {
        // Extract S3 key from R2 URL
        // URL format: https://endpoint/bucket/key
        String[] urlParts = fileUrl.split("/");
        if (urlParts.length >= 4) {
            StringBuilder key = new StringBuilder();
            for (int i = 4; i < urlParts.length; i++) {
                if (key.length() > 0) {
                    key.append("/");
                }
                key.append(urlParts[i]);
            }
            return key.toString();
        }
        throw new IllegalArgumentException("Invalid R2 URL format: " + fileUrl);
    }
}
