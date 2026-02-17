package com.plotterpro.controller;

import com.plotterpro.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "success", required = false) String success,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password. Please check your credentials.");
        }
        if (success != null) {
            model.addAttribute("success", "Operation successful! Please log in.");
        }
        if (logout != null) {
            model.addAttribute("success", "You have been logged out.");
        }
        return "login";
    }

    // POST /login is handled by Spring Security automatically

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email,
            @RequestParam String password,
            @RequestParam("confirm-password") String confirmPassword,
            Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "register";
        }
        try {
            userService.register(email, password);
            return "redirect:/login?success";
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/forgot-password")
    public String forgotPasswordPage() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, Model model) {
        try {
            userService.createPasswordResetToken(email);
            model.addAttribute("message", "If an account exists, a reset link has been sent to your email.");
        } catch (Exception e) {
            e.printStackTrace();
            // Show the real error to help debug
            model.addAttribute("error", "Error sending email: " + e.getMessage());
        }
        return "forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPasswordPage(@RequestParam String token, Model model) {
        if (!userService.validatePasswordResetToken(token)) {
            model.addAttribute("error", "Invalid or expired token");
            // Since we are redirecting to login, we should probably pass the error as a
            // param or flash attribute
            // But for now, let's just return to login view with error in model if that
            // works,
            // otherwise redirecting with param is safer for a fresh request.
            // Let's stick to the previous plan: return "login" view directly is fine if we
            // want to show error context.
            return "login";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String processResetPassword(@RequestParam String token,
            @RequestParam String password,
            @RequestParam("confirm-password") String confirmPassword,
            Model model) {
        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            model.addAttribute("token", token);
            return "reset-password";
        }

        try {
            userService.resetPassword(token, password);
            return "redirect:/login?success=Password+reset+successful";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to reset password: " + e.getMessage());
            model.addAttribute("token", token);
            return "reset-password";
        }
    }
}
