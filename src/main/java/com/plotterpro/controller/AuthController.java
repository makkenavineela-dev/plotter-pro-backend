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
            // Pass email to the view so it can be used in the hidden field forms
            model.addAttribute("email", email);
            model.addAttribute("success", "A 5-digit PIN has been sent to your email.");
            return "verify-pin"; // Show the PIN entry page directly
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Error sending email: " + e.getMessage());
            return "forgot-password";
        }
    }

    // New Endpoint: Verify PIN and Reset Password in one go
    @GetMapping("/verify-pin")
    public String verifyPinPage() {
        return "verify-pin";
    }

    @PostMapping("/verify-pin")
    public String processVerifyPin(@RequestParam String email,
            @RequestParam String pin,
            @RequestParam String password,
            Model model) {

        // 1. Validate PIN
        if (!userService.validatePasswordResetToken(pin)) {
            model.addAttribute("error", "Invalid or expired PIN.");
            model.addAttribute("email", email); // Keep email for retry
            return "verify-pin";
        }

        // 2. Reset Password
        try {
            userService.resetPassword(pin, password);
            return "redirect:/login?success=Password+reset+successful";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Failed to reset password: " + e.getMessage());
            model.addAttribute("email", email);
            return "verify-pin";
        }
    }

    // REMOVING OLD LINK-BASED ENDPOINTS IF THEY EXIST OR LEAVING THEM UNUSED
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
