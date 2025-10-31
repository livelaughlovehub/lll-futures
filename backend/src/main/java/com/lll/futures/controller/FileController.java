package com.lll.futures.controller;

import com.lll.futures.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * File upload controller for handling file operations
 */
@RestController
@RequestMapping("/api/files")
public class FileController {
    
    @Autowired
    private StorageService storageService;
    
    /**
     * Upload a file
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false) String folder) {
        
        try {
            String fileUrl = storageService.uploadFile(file, folder);
            
            Map<String, String> response = new HashMap<>();
            response.put("success", "true");
            response.put("fileUrl", fileUrl);
            response.put("message", "File uploaded successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("success", "false");
            response.put("message", "Failed to upload file: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Delete a file
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, String>> deleteFile(@RequestParam("fileUrl") String fileUrl) {
        
        try {
            boolean deleted = storageService.deleteFile(fileUrl);
            
            Map<String, String> response = new HashMap<>();
            response.put("success", String.valueOf(deleted));
            response.put("message", deleted ? "File deleted successfully" : "File not found");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, String> response = new HashMap<>();
            response.put("success", "false");
            response.put("message", "Failed to delete file: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * List files in a folder
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listFiles(
            @RequestParam(value = "folder", required = false) String folder) {
        
        try {
            var files = storageService.listFiles(folder);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", "true");
            response.put("files", files);
            response.put("count", files.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", "false");
            response.put("message", "Failed to list files: " + e.getMessage());
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Serve uploaded files
     */
    @GetMapping("/serve/**")
    public ResponseEntity<InputStreamResource> serveFile() {
        try {
            // Get the request object
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
                .getRequest();
            
            // Extract the file path from the request URI
            String requestPath = request.getRequestURI();
            String filePath = requestPath.replace("/api/files/serve", "");
            
            // Get file stream from storage service
            var inputStream = storageService.getFileStream(filePath);
            
            // Determine content type based on file extension
            String contentType = "application/octet-stream";
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            String lowerFileName = fileName.toLowerCase();
            
            if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
                contentType = MediaType.IMAGE_JPEG_VALUE;
            } else if (lowerFileName.endsWith(".png")) {
                contentType = MediaType.IMAGE_PNG_VALUE;
            } else if (lowerFileName.endsWith(".gif")) {
                contentType = MediaType.IMAGE_GIF_VALUE;
            } else if (lowerFileName.endsWith(".ico")) {
                contentType = "image/x-icon";
            } else if (lowerFileName.endsWith(".webp")) {
                contentType = "image/webp";
            } else if (lowerFileName.endsWith(".svg")) {
                contentType = "image/svg+xml";
            }
            
            InputStreamResource resource = new InputStreamResource(inputStream);
            
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);
                    
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
