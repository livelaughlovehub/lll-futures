package com.lll.futures.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.List;

/**
 * Storage service interface for handling file uploads
 * Supports both local file system and Cloudflare R2
 */
public interface StorageService {
    
    /**
     * Upload a file and return the file URL
     * @param file MultipartFile to upload
     * @param folder Optional folder path
     * @return File URL for access
     */
    String uploadFile(MultipartFile file, String folder);
    
    /**
     * Upload a file with custom filename
     * @param file MultipartFile to upload
     * @param folder Optional folder path
     * @param filename Custom filename
     * @return File URL for access
     */
    String uploadFile(MultipartFile file, String folder, String filename);
    
    /**
     * Delete a file by URL
     * @param fileUrl URL of the file to delete
     * @return true if successful
     */
    boolean deleteFile(String fileUrl);
    
    /**
     * Get file input stream for reading
     * @param fileUrl URL of the file
     * @return InputStream for the file
     */
    InputStream getFileStream(String fileUrl);
    
    /**
     * List files in a folder
     * @param folder Folder path
     * @return List of file URLs
     */
    List<String> listFiles(String folder);
    
    /**
     * Check if file exists
     * @param fileUrl URL of the file
     * @return true if file exists
     */
    boolean fileExists(String fileUrl);
}
