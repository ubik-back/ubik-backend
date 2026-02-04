package com.ubik.usermanagement.application.usecase;

import com.ubik.usermanagement.application.port.in.UserUseCase;
import com.ubik.usermanagement.application.port.out.UserRepositoryPort;
import com.ubik.usermanagement.domain.model.User;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.LoginRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.RegisterRequest;
import com.ubik.usermanagement.infrastructure.adapter.in.web.dto.ResetPasswordRequest;
import com.ubik.usermanagement.infrastructure.adapter.out.jwt.JwtAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servicio de gestión de usuarios
 * 
 * Principio SOLID: Single Responsibility - Solo maneja lógica de negocio de usuarios
 */
@Service
public class UserService implements UserUseCase {

    private final UserRepositoryPort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtAdapter jwtAdapter;

    public UserService(
            UserRepositoryPort userRepository, 
            PasswordEncoder passwordEncoder, 
            JwtAdapter jwtAdapter) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtAdapter = jwtAdapter;
    }

    @Override
    public Mono<String> register(RegisterRequest request) {
        return userRepository.findByUsername(request.username())
                .flatMap(existing -> Mono.<String>error(
                    new RuntimeException("Username already exists")))
                .switchIfEmpty(
                        userRepository.findByEmail(request.email())
                                .flatMap(existing -> Mono.<String>error(
                                    new RuntimeException("Email already exists")))
                                .switchIfEmpty(Mono.defer(() -> {

                                    User user = new User(
                                            null,
                                            request.username(),
                                            passwordEncoder.encode(request.password()),
                                            request.email(),
                                            null,                    // phoneNumber
                                            null,                    // createdAt
                                            request.anonymous(),
                                            request.roleId(),
                                            null,                    // resetToken
                                            null,                    // resetTokenExpiry
                                            request.longitude(),     // Nueva propiedad
                                            request.latitude(),      // Nueva propiedad
                                            request.birthDate()      // Nueva propiedad
                                    );

                                    return userRepository.save(user)
                                            .map(saved -> jwtAdapter.generateToken(
                                                    saved.username(),
                                                    saved.roleId()
                                            ));
                                }))
                );
    }

    @Override
    public Mono<String> login(LoginRequest request) {
        return userRepository.findByUsername(request.username())
                .filter(user -> passwordEncoder.matches(request.password(), user.password()))
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid credentials")))
                .map(user -> jwtAdapter.generateToken(
                        user.username(),
                        user.roleId()
                ));
    }

    @Override
    public Mono<String> requestPasswordReset(String email) {
        String resetToken = UUID.randomUUID().toString();

        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new RuntimeException("Email not found")))
                .flatMap(user -> userRepository.save(new User(
                        user.id(),
                        user.username(),
                        user.password(),
                        user.email(),
                        user.phoneNumber(),
                        user.createdAt(),
                        user.anonymous(),
                        user.roleId(),
                        resetToken,
                        LocalDateTime.now().plusHours(1),
                        user.longitude(),      // Mantener valores existentes
                        user.latitude(),       // Mantener valores existentes
                        user.birthDate()       // Mantener valores existentes
                )))
                .map(user -> resetToken);
    }

    @Override
    public Mono<String> resetPassword(ResetPasswordRequest request) {
        return userRepository.findByResetToken(request.token())
                .filter(user -> user.resetTokenExpiry() != null 
                    && user.resetTokenExpiry().isAfter(LocalDateTime.now()))
                .switchIfEmpty(Mono.error(new RuntimeException("Invalid or expired token")))
                .flatMap(user -> userRepository.save(new User(
                        user.id(),
                        user.username(),
                        passwordEncoder.encode(request.newPassword()),
                        user.email(),
                        user.phoneNumber(),
                        user.createdAt(),
                        user.anonymous(),
                        user.roleId(),
                        null,
                        null,
                        user.longitude(),      // Mantener valores existentes
                        user.latitude(),       // Mantener valores existentes
                        user.birthDate()       // Mantener valores existentes
                )))
                .map(user -> "Password reset successfully");
    }
}