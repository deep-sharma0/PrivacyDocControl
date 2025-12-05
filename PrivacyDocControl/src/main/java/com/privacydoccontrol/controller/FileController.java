package com.privacydoccontrol.controller;

import com.privacydoccontrol.model.Document;
import com.privacydoccontrol.model.User;
import com.privacydoccontrol.security.CustomUserDetails;
import com.privacydoccontrol.service.FileService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Controller
public class FileController {

    @Autowired
    private FileService fileService;

    @GetMapping("/upload")
    public String showUploadPage() {
        return "upload";
    }

    @PostMapping("/upload")
    public String handleUpload(@RequestParam("file") MultipartFile file,
                               @AuthenticationPrincipal CustomUserDetails userDetails,
                               Model model) {
        try {
            User user = userDetails.getUser();
            Document doc = fileService.saveDocument(file, user);
            model.addAttribute("token", doc.getToken());
            return "upload_success";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Upload failed: " + e.getMessage());
            return "upload";
        }
    }

    // Secure file preview / download
    @GetMapping("/files/{filename:.+}")
    public void serveFile(@PathVariable String filename,
                          HttpServletResponse response) {
        try {
            // Set no-cache headers
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");

            // Encode filename for safe download
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-Disposition", "inline; filename=\"" + encodedFilename + "\"");

            // Stream file directly without storing in temp
            try (InputStream in = fileService.loadFileAsStream(filename)) {
                in.transferTo(response.getOutputStream());
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }
}
