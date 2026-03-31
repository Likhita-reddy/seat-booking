package com.seatbooking.users_service;

import com.seatbooking.users_service.dto.LoginRequest;
import com.seatbooking.users_service.dto.RegisterRequest;
import com.seatbooking.users_service.entity.User;
import com.seatbooking.users_service.exception.EmailAlreadyExistsException;
import com.seatbooking.users_service.exception.InvalidCredentialsException;
import com.seatbooking.users_service.repository.UserRepository;
import com.seatbooking.users_service.service.AuthService;
import com.seatbooking.users_service.service.JwtService;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_success(){
        RegisterRequest request = new RegisterRequest();
        request.setName("Test");
        request.setEmail("test@gmail.com");
        request.setPassword("test");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashedPassword");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setEmail("test@gmail.com");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(1L,"test@gmail.com")).thenReturn("token");

        var response = authService.register(request);

        assertEquals("test@gmail.com",response.getEmail());
        assertEquals(1L, response.getUserId());
        verify(userRepository).save(any(User.class));

    }

    @Test
    void register_emailAlreadyExists_throwsException(){
        RegisterRequest request = new RegisterRequest();

        request.setName("test");
        request.setEmail("test@gmail.com");
        request.setPassword("test");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,()->authService.register(request));
        verify(userRepository,never()).save(any());
    }

    @Test
    void login_success(){
        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("test");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@gmail.com");
        user.setPassword("test");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("test","test")).thenReturn(true);
        when(jwtService.generateToken(1L,request.getEmail())).thenReturn("token");

        var response = authService.login(request);

        assertEquals("token",response.getToken());
        assertEquals("test@gmail.com",response.getEmail());
    }

    @Test
    void login_wrongPassword_throwsException(){
        LoginRequest request = new LoginRequest();
        request.setEmail("test@gmail.com");
        request.setPassword("tes");

        User user = new User();
        user.setId(1L);
        user.setPassword("test");
        user.setEmail("test@gmail.com");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("tes","test")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,()->authService.login(request));
    }

    @Test
    void login_userNotFound_throwsException() {
        LoginRequest request = new LoginRequest();
        request.setEmail("notfound@gmail.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("notfound@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> authService.login(request));
    }
}
