package com.isaborosa.biblioteca.security;

import com.isaborosa.biblioteca.domain.user.User;
import com.isaborosa.biblioteca.domain.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gera o hash BCrypt da senha inicial no primeiro start, em vez de guardar
 * qualquer senha em texto puro em SQL ou no codigo. So roda quando o usuario
 * seed ainda esta com password_hash vazio (ou seja, uma unica vez).
 */
@Component
class InitialUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialUserSeeder.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String initialUsername;
    private final String initialPassword;

    InitialUserSeeder(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.auth.initial-username}") String initialUsername,
            @Value("${app.auth.initial-password}") String initialPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.initialUsername = initialUsername;
        this.initialPassword = initialPassword;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        User user = userRepository.findByUsername(initialUsername).orElseGet(() -> {
            // Perfil de teste (H2, sem Flyway): a linha seed da migration V1
            // nunca roda, entao o usuario nem existe ainda - criamos aqui.
            log.info("Usuario '{}' nao encontrado; criando com a senha inicial.", initialUsername);
            return userRepository.save(new User(initialUsername, initialUsername, "", true));
        });

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            user.initializePassword(passwordEncoder.encode(initialPassword));
            userRepository.save(user);
            log.info("Senha inicial do usuario '{}' definida. Troca sera exigida no proximo login.", initialUsername);
        }
    }
}
