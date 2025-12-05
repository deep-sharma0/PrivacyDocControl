package com.privacydoccontrol.service;

import com.privacydoccontrol.model.Document;
import com.privacydoccontrol.repository.DocumentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class DocumentService {

    @Value("${upload.dir}")
    private String uploadDir;

    @Autowired
    private DocumentRepository repo;

    @Autowired
    private PrinterService printerService;

    private static final List<String> ALLOWED_EXTENSIONS =
            Arrays.asList(".pdf", ".doc", ".docx", ".txt", ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".tiff", ".svg");

    @Transactional
    public String saveDocument(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be null or empty.");
        }

        // Validate file extension
        String fileExtension = originalFilename.substring(originalFilename.lastIndexOf(".")).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new IllegalArgumentException("Only allowed file types: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        // Generate token
        String token = UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String fileName = token + "_" + originalFilename;
        String filePath = uploadDir + File.separator + fileName;

        // Save file to disk
        File destination = new File(filePath);
        destination.getParentFile().mkdirs();
        file.transferTo(destination);

        // Save document metadata in DB
        Document doc = new Document();
        doc.setToken(token);
        doc.setFileName(fileName);
        doc.setFilePath(filePath);
        doc.setUploadedAt(LocalDateTime.now());
        doc.setExpiresAt(doc.getUploadedAt().plusMinutes(15));
        doc.setPrinted(false);
        doc.setExpired(false);

        repo.save(doc);
        log.info("Document saved: {} with token {}", fileName, token);
        return token;
    }

    public Document getDocumentByToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            log.warn("Attempted to retrieve document with empty token");
            return null;
        }

        String processedToken = token.trim();
        Document doc = repo.findByTokenIgnoreCase(processedToken);

        if (doc == null) {
            log.warn("No document found for token {}", processedToken);
            return null;
        }

        // Check expiration
        if (doc.getExpiresAt() == null || doc.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.info("Document token {} expired at {}", processedToken, doc.getExpiresAt());
            doc.setExpired(true);
            repo.save(doc);
            return null;
        }

        return doc;
    }

    public boolean printAndDeleteDocument(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }

        Document doc = repo.findByTokenIgnoreCase(token.trim());
        if (doc == null || doc.isPrinted() || doc.isExpired()) {
            return false;
        }

        // Print the document
        boolean printed = printerService.printFile(doc.getFilePath());
        if (!printed) {
            log.warn("Failed to print document with token: {}", token);
            // Even if printing fails, we still mark as printed and delete for security reasons
        }

        // Mark as printed
        doc.setPrinted(true);
        repo.save(doc);

        // Securely delete file
        File file = new File(doc.getFilePath());
        if (file.exists()) {
            secureDelete(file);
        } else {
            log.warn("File for token {} not found during deletion: {}", token, doc.getFilePath());
        }

        log.info("Document with token {} printed and deleted", token);
        return true;
    }

    @Scheduled(fixedRate = 60000) // every minute
    public void cleanupExpiredDocuments() {
        List<Document> expiredDocuments = repo.findAll().stream()
                .filter(doc -> !doc.isExpired() && doc.getExpiresAt().isBefore(LocalDateTime.now()))
                .toList();

        for (Document doc : expiredDocuments) {
            doc.setExpired(true);
            repo.save(doc);

            File file = new File(doc.getFilePath());
            if (file.exists()) {
                secureDelete(file);
            } else {
                log.warn("File for expired document not found: {}", doc.getFilePath());
            }

            log.info("Expired document cleaned: {}", doc.getFileName());
        }
    }

    /**
     * Securely overwrite file contents with random data before deleting.
     */
    private void secureDelete(File file) {
        try (RandomAccessFile raf = new RandomAccessFile(file, "rws")) {
            long length = file.length();
            byte[] data = new byte[4096];
            SecureRandom random = new SecureRandom();
            long pos = 0;
            while (pos < length) {
                random.nextBytes(data);
                raf.write(data, 0, (int) Math.min(data.length, length - pos));
                pos += data.length;
            }
        } catch (IOException e) {
            log.error("Error securely overwriting file {}: {}", file.getAbsolutePath(), e.getMessage());
        }

        if (!file.delete()) {
            log.error("Failed to delete file: {}", file.getAbsolutePath());
        }
    }
}
