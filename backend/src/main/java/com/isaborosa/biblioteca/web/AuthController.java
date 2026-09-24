package com.isaborosa.biblioteca.web;

import com.isaborosa.biblioteca.domain.user.User;
import com.isaborosa.biblioteca.dto.ChangePasswordRequest;
import com.isaborosa.biblioteca.dto.LoginRequest;
import com.isaborosa.biblioteca.dto.UserSessionDto;
import com.isaborosa.biblioteca.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacao", description = "Login por sessao (cookie), troca de senha obrigatoria no primeiro acesso")
public class AuthController {

    private final AuthService authService;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(AuthService authService, SecurityContextRepository securityContextRepository) {
        this.authService = authService;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/login")
    @Operation(summary = "Login por usuario e senha", description = "Cria uma sessao (cookie). Resposta indica se a troca de senha e obrigatoria.")
    public UserSessionDto login(
            @Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        User user = authService.authenticate(request.username(), request.password());

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getUsername(), null, AuthorityUtils.NO_AUTHORITIES);
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, httpRequest, httpResponse);

        return toSessionDto(user);
    }

    @PostMapping("/logout")
    @Operation(summary = "Encerra a sessao atual")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Dados da sessao atual")
    public UserSessionDto me(Authentication authentication) {
        User user = authService.requireByUsername(authentication.getName());
        return toSessionDto(user);
    }

    @PostMapping("/change-password")
    @Operation(summary = "Troca a senha do usuario logado", description = "Exige a senha atual; usada tanto na troca obrigatoria do primeiro login quanto depois, por escolha do usuario.")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request, Authentication authentication) {
        authService.changePassword(authentication.getName(), request.currentPassword(), request.newPassword());
        return ResponseEntity.noContent().build();
    }

    private UserSessionDto toSessionDto(User user) {
        return new UserSessionDto(user.getUsername(), user.getName(), user.isMustChangePassword());
    }
}
