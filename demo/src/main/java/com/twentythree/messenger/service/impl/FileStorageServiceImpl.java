package com.twentythree.messenger.service.impl;

import com.twentythree.messenger.exception.FileStorageException;
import com.twentythree.messenger.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;


import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageServiceImpl(@Value("${file.upload-dir}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    @Override
    public String storeFile(MultipartFile file, String subfolder) {
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String fileExtension = "";
        try {
            if (originalFileName.contains(".")) {
                fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            // Generate a unique file name to prevent overwrites and sanitize
            String uniqueFileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + fileExtension;
            
            Path targetLocationPath = this.fileStorageLocation;
            if (subfolder != null && !subfolder.isBlank()) {
                targetLocationPath = this.fileStorageLocation.resolve(subfolder);
                Files.createDirectories(targetLocationPath); // Ensure subfolder exists
            }
            targetLocationPath = targetLocationPath.resolve(uniqueFileName);

            Files.copy(file.getInputStream(), targetLocationPath, StandardCopyOption.REPLACE_EXISTING);

            // Return the path relative to the main upload directory (including subfolder if used)
            if (subfolder != null && !subfolder.isBlank()) {
                return Paths.get(subfolder).resolve(uniqueFileName).toString().replace("\\", "/");
            }
            return uniqueFileName;

        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + originalFileName + ". Please try again!", ex);
        }
    }

    @Override
    public Resource loadFileAsResource(String fileNameWithSubfolder) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileNameWithSubfolder).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new FileStorageException("File not found " + fileNameWithSubfolder);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("File not found " + fileNameWithSubfolder, ex);
        }
    }
    
    @Override
    public void deleteFile(String fileName, String subfolder) throws IOException {
         try {
            Path targetLocationPath = this.fileStorageLocation;
            if (subfolder != null && !subfolder.isBlank()) {
                targetLocationPath = this.fileStorageLocation.resolve(subfolder);
            }
            Path filePath = targetLocationPath.resolve(fileName).normalize();
            Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            throw new FileStorageException("Could not delete file " + fileName, ex);
        }
    }
}