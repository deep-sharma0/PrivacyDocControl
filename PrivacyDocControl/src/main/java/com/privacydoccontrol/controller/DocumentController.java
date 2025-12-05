package com.privacydoccontrol.controller;

import com.privacydoccontrol.model.Document;
import com.privacydoccontrol.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

@Controller
public class DocumentController {

    @Autowired
    private DocumentRepository documentRepository;

    // Serve file by token with 15-minute expiration
    @GetMapping("/files/{token}")
    @ResponseBody
    public ResponseEntity<Resource> serveFile(@PathVariable String token) throws IOException {
        Document document = documentRepository.findByToken(token);

        if (document == null || document.isPrinted() || document.isExpired()) {
            return ResponseEntity.notFound().build();
        }

        // Check expiration
        if (document.getExpiresAt().isBefore(LocalDateTime.now())) {
            cleanupAndRemove(document);
            return ResponseEntity.notFound().build();
        }

        File file = new File(document.getFilePath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        FileSystemResource resource = new FileSystemResource(file);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + document.getFileName() + "\"");

        // 🔒 Prevent browser caching
        headers.add(HttpHeaders.CACHE_CONTROL, "no-cache, no-store, must-revalidate");
        headers.add(HttpHeaders.PRAGMA, "no-cache");
        headers.add(HttpHeaders.EXPIRES, "0");

        // Detect content type
        String fileName = document.getFileName().toLowerCase();
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (fileName.endsWith(".pdf")) mediaType = MediaType.APPLICATION_PDF;
        else if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) mediaType = MediaType.IMAGE_JPEG;
        else if (fileName.endsWith(".png")) mediaType = MediaType.IMAGE_PNG;
        else if (fileName.endsWith(".gif")) mediaType = MediaType.IMAGE_GIF;
        else if (fileName.endsWith(".txt")) mediaType = MediaType.TEXT_PLAIN;

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(mediaType)
                .body(resource);
    }

    // Print and delete document
    @PostMapping("/staff/print-and-delete")
    public String printAndDelete(@RequestParam("token") String token) {
        Document document = documentRepository.findByToken(token);
        if (document != null) {
            // Mark as printed
            document.setPrinted(true);
            documentRepository.save(document);

            cleanupAndRemove(document);
        }
        return "redirect:/staff"; // back to staff dashboard
    }

    // 🔒 Helper: securely delete file and remove from DB
    private void cleanupAndRemove(Document document) {
        File file = new File(document.getFilePath());
        if (file.exists()) {
            file.delete(); // you could replace with a secure delete method if needed
        }
        documentRepository.delete(document);
    }
}
