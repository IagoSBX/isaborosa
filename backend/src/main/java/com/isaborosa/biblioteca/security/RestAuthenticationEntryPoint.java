package com.isaborosa.biblioteca.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isaborosa.biblioteca.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * Sem isso, o Spring Security devolveria uma pagina de login HTML padrao (ou
 * um 403 sem corpo) para uma chamada de API sem sessao. Aqui a resposta segue
 * o mesmo formato ApiErrorResponse do resto da API, com 401 - o frontend usa
 * isso pra saber "sessao expirou, manda pro login" em vez de tratar como um
 * erro generico.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiErrorResponse body = new ApiErrorResponse("UNAUTHENTICATED", "Sessão expirada ou inexistente. Faça login novamente.");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}
