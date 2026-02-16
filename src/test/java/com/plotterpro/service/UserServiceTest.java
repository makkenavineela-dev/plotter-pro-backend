package com.plotterpro.service;

import com.plotterpro.entity.UserEntity;
import com.plotterpro.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    public void testRegister_Success() {
        String email = "test@example.com";
        String password = "password123";
        String encodedPassword = "encodedPassword123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(userRepository.save(any(UserEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        UserEntity registeredUser = userService.register(email, password);

        assertNotNull(registeredUser);
        assertEquals(email, registeredUser.getEmail());
        assertEquals(encodedPassword, registeredUser.getPasswordHash());
        verify(userRepository, times(1)).save(any(UserEntity.class));
    }

    @Test
    public void testRegister_UserAlreadyExists() {
        String email = "existing@example.com";
        String password = "password123";

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new UserEntity(email, "hash")));

        Exception exception = assertThrows(RuntimeException.class, () -> {
            userService.register(email, password);
        });

        assertEquals("User already exists", exception.getMessage());
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    public void testLoadUserByUsername_Success() {
        String email = "user@example.com";
        String passwordHash = "encodedHash";
        UserEntity user = new UserEntity(email, passwordHash);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername(email);

        assertNotNull(userDetails);
        assertEquals(email, userDetails.getUsername());
        assertEquals(passwordHash, userDetails.getPassword());
    }

    @Test
    public void testLoadUserByUsername_NotFound() {
        String email = "nonexistent@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> {
            userService.loadUserByUsername(email);
        });
    }

    @Test
    public void testDeleteAccount_Success() {
        String email = "delete@example.com";
        UserEntity user = new UserEntity(email, "hash");

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        userService.deleteAccount(email);

        verify(userRepository, times(1)).delete(user);
    }
}
