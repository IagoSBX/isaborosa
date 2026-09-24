package com.isaborosa.biblioteca.service;

import com.isaborosa.biblioteca.domain.user.User;
import com.isaborosa.biblioteca.domain.user.UserRepository;
import com.isaborosa.biblioteca.exception.InvalidCredentialsException;
import java.time.Instant;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Mesma mensagem de erro para usuario inexistente, inativo ou senha
     * incorreta - nunca revela qual dos tres aconteceu.
     */
    @Transactional
    public User authenticate(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Usuário ou senha inválidos"));

        if (!user.isActive() || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Usuário ou senha inválidos");
        }

        user.recordLogin(Instant.now());
        return user;
    }

    @Transactional(readOnly = true)
    public User requireByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Usuário ou senha inválidos"));
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new InvalidCredentialsException("Usuário ou senha inválidos"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException("Senha atual incorreta");
        }

        user.changePassword(passwordEncoder.encode(newPassword));
    }
}
