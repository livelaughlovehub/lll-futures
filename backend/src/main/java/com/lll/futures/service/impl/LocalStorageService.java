package com.lll.futures.service.impl;

import com.lll.futures.service.StorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Local file system storage implementation
 * Used for development environment
 */
@Service("localStorageService")
public class LocalStorageService implements StorageService {
    
    @Value("${storage.local.path:./uploads}")
    private String localStoragePath;
    
    @Override
    public String uploadFile(MultipartFile file, String folder) {
        return uploadFile(file, folder, null);
    }
    
    @Override
    public String uploadFile(MultipartFile file, String folder, String filename) {
        try {
            // Create directory if it doesn't exist
            Path uploadPath = Paths.get(localStoragePath);
            if (folder != null && !folder.isEmpty()) {
                uploadPath = uploadPath.resolve(folder);
            }
            Files.createDirectories(uploadPath);
            
            // Generate filename if not provided
            if (filename == null || filename.isEmpty()) {
                String originalFilename = file.getOriginalFilename();
                String extension = "";
                if (originalFilename != null && originalFilename.contains(".")) {
                    extension = originalFilename.substring(originalFilename.lastIndexOf("."));
                }
                filename = UUID.randomUUID().toString() + extension;
            }
            
            // Save file
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
            
            // Return URL that the frontend can use to access the file
            // The URL will be relative to the backend API and can be used with the file serving endpoint
            String relativePath;
            if (folder != null && !folder.isEmpty()) {
                relativePath = "/uploads/" + folder + "/" + filename;
            } else {
                relativePath = "/uploads/" + filename;
            }
            
            // Return the path that will work with the /api/files/serve endpoint
            return relativePath;
            
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean deleteFile(String fileUrl) {
        try {
            // Convert URL to file path
            String filePath = fileUrl.replace("/uploads/", localStoragePath + "/");
            Path path = Paths.get(filePath);
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            return false;
        }
    }
    
    @Override
    public InputStream getFileStream(String fileUrl) {
        try {
            String filePath = fileUrl.replace("/uploads/", localStoragePath + "/");
            return Files.newInputStream(Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException("Failed to read file: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<String> listFiles(String folder) {
        List<String> files = new ArrayList<>();
        try {
            final Path folderPath = Paths.get(localStoragePath);
            final Path targetPath = folder != null && !folder.isEmpty() 
                ? folderPath.resolve(folder) 
                : folderPath;
            
            if (Files.exists(targetPath)) {
                Files.walk(targetPath)
                    .filter(Files::isRegularFile)
                    .forEach(path -> {
                        String relativePath = targetPath.relativize(path).toString();
                        files.add("/uploads/" + (folder != null ? folder + "/" : "") + relativePath);
                    });
            }
        } catch (IOException e) {
            // Return empty list on error
        }
        return files;
    }
    
    @Override
    public boolean fileExists(String fileUrl) {
        String filePath = fileUrl.replace("/uploads/", localStoragePath + "/");
        return Files.exists(Paths.get(filePath));
    }
}
