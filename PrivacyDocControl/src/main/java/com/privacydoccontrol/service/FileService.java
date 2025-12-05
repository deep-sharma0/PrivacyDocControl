package com.privacydoccontrol.service;

import com.privacydoccontrol.model.Document;
import com.privacydoccontrol.model.User;
import com.privacydoccontrol.repository.DocumentRepository;
import com.privacydoccontrol.util.TokenGenerator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

@Service
public class FileService {

    @Autowired
    private DocumentRepository docRepo;

    private final String uploadDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

    public Document saveDocument(MultipartFile file, User user) throws IOException {
        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = System.currentTimeMillis() + "_" + originalFileName;
        String fullPath = uploadDir + uniqueFileName;

        File dest = new File(fullPath);
        dest.getParentFile().mkdirs(); // Ensure directory exists
        file.transferTo(dest);

        System.out.println("Saved file to: " + fullPath);

        Document doc = new Document();
        doc.setFileName(originalFileName);
        doc.setFilePath(fullPath);
        doc.setToken(TokenGenerator.generateToken());
        doc.setUser(user);
        doc.setUploadedAt(LocalDateTime.now());
       doc.setExpiresAt(doc.getUploadedAt().plusMinutes(15));
        doc.setPrinted(false);

        return docRepo.save(doc);
    }

    public InputStream loadFileAsStream(String fileName) throws IOException {
    File file = new File(uploadDir + File.separator + fileName);
    if (!file.exists()) {
        throw new IOException("File not found: " + fileName);
    }
    return new FileInputStream(file);
}
}
