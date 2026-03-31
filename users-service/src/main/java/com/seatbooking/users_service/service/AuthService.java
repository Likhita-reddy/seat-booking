package com.seatbooking.users_service.service;

import com.seatbooking.users_service.dto.AuthResponse;
import com.seatbooking.users_service.dto.LoginRequest;
import com.seatbooking.users_service.dto.RegisterRequest;
import com.seatbooking.users_service.entity.User;
import com.seatbooking.users_service.exception.EmailAlreadyExistsException;
import com.seatbooking.users_service.exception.InvalidCredentialsException;
import com.seatbooking.users_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new EmailAlreadyExistsException("Email "+ request.getEmail() + " already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();

        User saved = userRepository.save(user);
        String token = jwtService.generateToken(saved.getId(),saved.getEmail());
        return new AuthResponse(token, saved.getEmail(),saved.getId());
    }

    public AuthResponse login(LoginRequest request){
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(()-> new InvalidCredentialsException("Invalid email or password"));

        if(!passwordEncoder.matches(request.getPassword(),user.getPassword())){
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId(),user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getId());
    }
}
