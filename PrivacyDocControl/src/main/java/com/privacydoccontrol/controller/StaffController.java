package com.privacydoccontrol.controller;

import com.privacydoccontrol.model.Document;
import com.privacydoccontrol.service.DocumentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Controller
@RequestMapping("/staff")
public class StaffController {

    @Autowired
    private DocumentService docService;

    @GetMapping("/view")
    public String showTokenForm() {
        return "staff_token";
    }

    @PostMapping("/view")
    public String viewDocument(@RequestParam("token") String token, Model model) {
        log.info("Staff requested to view document with token: {}", token);

        if (token == null || token.trim().isEmpty()) {
            model.addAttribute("error", "Token cannot be empty.");
            return "staff_token";
        }

        try {
            Document doc = docService.getDocumentByToken(token);

            if (doc == null) {
                model.addAttribute("error", "Invalid token or document has expired.");
                return "staff_token";
            }

            if (doc.isPrinted()) {
                model.addAttribute("error", "This document has already been printed.");
                return "staff_token";
            }

            if (doc.isTokenExpired() || doc.getExpiresAt().isBefore(LocalDateTime.now())) {
                model.addAttribute("error", "This document has expired and is no longer available.");
                return "staff_token";
            }

            // Calculate remaining time
            long minutesRemaining = Duration.between(LocalDateTime.now(), doc.getExpiresAt()).toMinutes();

            model.addAttribute("doc", doc);
            model.addAttribute("minutesRemaining", minutesRemaining);
            model.addAttribute("expiresAt", doc.getExpiresAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            model.addAttribute("success", "Document found successfully!");
            return "staff_view";

        } catch (Exception e) {
            log.error("Error while viewing document with token {}: {}", token, e.getMessage(), e);
            model.addAttribute("error", "An unexpected error occurred. Please try again.");
            return "staff_token";
        }
    }

    @PostMapping("/print")
    public String printAndDelete(@RequestParam("token") String token, Model model) {
        log.info(">>> printAndDelete called with token: '{}'", token);

        if (token == null || token.trim().isEmpty()) {
            model.addAttribute("error", "Token cannot be empty.");
            return "staff_token";
        }

        boolean success = docService.printAndDeleteDocument(token);

        if (!success) {
            log.warn("Failed to print document with token: {}", token);
            model.addAttribute("error", "Unable to print. Token is invalid, expired, or already printed.");
            return "staff_token";
        }

        log.info("Document printed and deleted successfully for token: {}", token);
        model.addAttribute("success", "Document printed successfully and has been removed from the system.");

        // Redirect back to view page for new token input
        return "redirect:/staff/view";
    }
}
