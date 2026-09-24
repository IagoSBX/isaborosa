package com.isaborosa.biblioteca.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Autenticacao baseada em sessao (cookie JSESSIONID), nao token. CSRF fica
 * desligado porque a API so aceita JSON de origens explicitas na lista de
 * CORS (nunca form-encoded, nunca "*") e o cookie de sessao usa SameSite
 * padrao (Lax) - suficiente para o cenario de front/back em portas
 * diferentes do mesmo host, sem expor a um ataque CSRF classico via form.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_GET_PATHS = {
            "/actuator/health", "/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**"
    };

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http, CorsConfigurationSource corsConfigurationSource, RestAuthenticationEntryPoint entryPoint,
            SecurityContextRepository securityContextRepository)
            throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .securityContext(context -> context.securityContextRepository(securityContextRepository))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(entryPoint))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PUBLIC_GET_PATHS).permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
