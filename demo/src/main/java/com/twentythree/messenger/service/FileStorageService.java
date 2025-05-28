package com.twentythree.messenger.service;

import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import org.springframework.core.io.Resource;


public interface FileStorageService {
    String storeFile(MultipartFile file, String subfolder);
    Resource loadFileAsResource(String fileNameWithSubfolder); // Potentially for serving files directly if not using static mapping
    void deleteFile(String fileName, String subfolder) throws Exception;
}