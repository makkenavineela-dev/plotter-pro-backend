package com.plotterpro.controller;

import com.plotterpro.entity.UserEntity;
import com.plotterpro.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String viewProfile(Model model, @AuthenticationPrincipal UserDetails userDetails) {
        UserEntity user = userService.getUserByEmail(userDetails.getUsername());
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/update-email")
    public String updateEmail(@RequestParam String email,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            userService.updateEmail(userDetails.getUsername(), email);
            redirectAttributes.addFlashAttribute("success", "Email updated successfully. Please log in again.");
            return "redirect:/logout"; // Force logout for security
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/profile";
        }
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam String newPassword,
            @RequestParam String confirmPassword,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        System.out.println("Processing password change for: " + userDetails.getUsername());

        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Password mismatch");
            redirectAttributes.addFlashAttribute("error", "Passwords do not match");
            return "redirect:/profile";
        }

        try {
            userService.updatePassword(userDetails.getUsername(), newPassword);
            System.out.println("Password updated successfully");
            redirectAttributes.addFlashAttribute("success", "Password changed successfully");
            return "redirect:/profile";
        } catch (Exception e) {
            System.out.println("Error updating password: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Failed to update password");
            return "redirect:/profile";
        }
    }

    @PostMapping("/delete-account")
    public String deleteAccount(@AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        try {
            userService.deleteAccount(userDetails.getUsername());
            redirectAttributes.addFlashAttribute("success", "Account deleted successfully");
            return "redirect:/logout";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete account");
            return "redirect:/profile";
        }
    }
}
