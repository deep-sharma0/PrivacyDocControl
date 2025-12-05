package com.privacydoccontrol.controller;

import com.privacydoccontrol.model.User;
import com.privacydoccontrol.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class LoginController {

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // Show custom login page
    @GetMapping("/login")
    public String loginPage() {
        return "login"; // login.html
    }

    // Show registration page
    @GetMapping("/register")
    public String registerPage(Model model) {
        model.addAttribute("user", new User());
        return "register"; // register.html
    }

    // Handle registration form submit
  @PostMapping("/register")
@Transactional
public String registerUser(
        @ModelAttribute("user") User user,
        @RequestParam("role") String role,
        Model model) {

    // Check if email already exists
    User existing = userRepo.findByEmail(user.getEmail());
    if (existing != null) {
        model.addAttribute("error", "Email already registered");
        return "register";
    }

    // Encode password
    user.setPassword(passwordEncoder.encode(user.getPassword()));

    // Normalize and set role
    if (!role.startsWith("ROLE_")) {
        role = "ROLE_" + role.toUpperCase();
    }
    user.setRole(role);

    // Save user
    userRepo.save(user);

    model.addAttribute("success", "Registration successful. Please login.");
    System.out.println("User registered: " + user.getEmail() + " with role: " + role);
    return "login";
}


    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard"; // Make sure dashboard.html exists in /templates
    }

}
