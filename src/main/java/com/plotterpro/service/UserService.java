package com.plotterpro.service;

import com.plotterpro.entity.UserEntity;
import com.plotterpro.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import org.springframework.beans.factory.annotation.Value;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Value("${app.base-url}")
    private String baseUrl;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // Implementing UserDetailsService REQUIRED method
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Convert our UserEntity to Spring Security's UserDetails
        return new User(
                user.getEmail(),
                user.getPasswordHash(),
                Collections.emptyList() // No roles/authorities for now
        );
    }

    public UserEntity register(String email, String rawPassword) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("User already exists");
        }
        // Hash the password before saving
        String hash = passwordEncoder.encode(rawPassword);
        return userRepository.save(new UserEntity(email, hash));
    }

    // Helper to get current user entity from repository
    public UserEntity getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public void updatePassword(String email, String newPassword) {
        UserEntity user = getUserByEmail(email);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void updateEmail(String currentEmail, String newEmail) {
        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new RuntimeException("Email already in use");
        }
        UserEntity user = getUserByEmail(currentEmail);
        user.setEmail(newEmail);
        userRepository.save(user);
    }

    public void deleteAccount(String email) {
        UserEntity user = getUserByEmail(email);
        userRepository.delete(user);
    }

    public void createPasswordResetToken(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            String token = java.util.UUID.randomUUID().toString();
            user.setResetPasswordToken(token);
            user.setResetPasswordTokenExpiry(java.time.LocalDateTime.now().plusHours(24)); // 24 hour expiry to fix
                                                                                           // timezone issues
            userRepository.save(user);

            // Construct link
            String resetLink = baseUrl + "/reset-password?token=" + token;

            // Send Email
            String subject = "PlotterPro: Password Reset Request";
            String text = "Hello,\n\n" +
                    "We received a request to reset the password for your PlotterPro account.\n" +
                    "Please click the link below to set a new password:\n\n" +
                    resetLink + "\n\n" +
                    "This link allows you to reset your password and is valid for 24 hours.\n" +
                    "If you did not request this change, you can safely ignore this email.\n\n" +
                    "Best regards,\n" +
                    "The PlotterPro Team";

            emailService.sendSimpleMessage(email, subject, text);

            System.out.println("Email sent to " + email);
        });
    }

    public boolean validatePasswordResetToken(String token) {
        return userRepository.findByResetPasswordToken(token)
                .map(user -> {
                    System.out.println("Validating Token: " + token);
                    System.out.println("Token Expiry: " + user.getResetPasswordTokenExpiry());
                    System.out.println("Current Time: " + java.time.LocalDateTime.now());
                    boolean isValid = user.getResetPasswordTokenExpiry().isAfter(java.time.LocalDateTime.now());
                    System.out.println("Is Valid? " + isValid);
                    return isValid;
                })
                .orElse(false);
    }

    public void resetPassword(String token, String newPassword) {
        UserEntity user = userRepository.findByResetPasswordToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setResetPasswordToken(null);
        user.setResetPasswordTokenExpiry(null);
        userRepository.save(user);
    }
}
