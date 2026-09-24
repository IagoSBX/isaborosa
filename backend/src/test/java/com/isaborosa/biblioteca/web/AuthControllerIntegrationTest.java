package com.isaborosa.biblioteca.web;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isaborosa.biblioteca.dto.ChangePasswordRequest;
import com.isaborosa.biblioteca.dto.LoginRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cobre o fluxo real de autenticacao: o InitialUserSeeder (ApplicationRunner)
 * ja roda no contexto de teste, entao o usuario "Isaborosa" existe com a
 * senha inicial "amor_da_minha_vida" e must_change_password=true antes de
 * qualquer teste aqui.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    private static final String INITIAL_USERNAME = "Isaborosa";
    private static final String INITIAL_PASSWORD = "amor_da_minha_vida";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void primeiroLoginFuncionaEIndicaTrocaDeSenhaObrigatoria() throws Exception {
        LoginRequest request = new LoginRequest(INITIAL_USERNAME, INITIAL_PASSWORD);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(INITIAL_USERNAME))
                .andExpect(jsonPath("$.mustChangePassword").value(true));
    }

    @Test
    void senhaErradaRetorna401SemRevelarOMotivo() throws Exception {
        LoginRequest request = new LoginRequest(INITIAL_USERNAME, "senha-errada");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }

    @Test
    void usuarioInexistenteRetorna401() throws Exception {
        LoginRequest request = new LoginRequest("naoexiste", "qualquer-coisa");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void endpointProtegidoSemSessaoRetorna401() throws Exception {
        mockMvc.perform(get("/api/library"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("UNAUTHENTICATED"));
    }

    @Test
    void loginTrocaDeSenhaELoginComANovaSenhaFuncionamEmSequencia() throws Exception {
        var session = login(INITIAL_USERNAME, INITIAL_PASSWORD);

        ChangePasswordRequest changeRequest = new ChangePasswordRequest(INITIAL_PASSWORD, "nova-senha-forte-123");
        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRequest))
                        .session(session))
                .andExpect(status().isNoContent());

        // a senha antiga (inicial) nao funciona mais
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(INITIAL_USERNAME, INITIAL_PASSWORD))))
                .andExpect(status().isUnauthorized());

        // a nova senha funciona e mustChangePassword agora e false
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(INITIAL_USERNAME, "nova-senha-forte-123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mustChangePassword").value(false));
    }

    @Test
    void trocaDeSenhaComSenhaAtualErradaERecusada() throws Exception {
        var session = login(INITIAL_USERNAME, INITIAL_PASSWORD);

        ChangePasswordRequest changeRequest = new ChangePasswordRequest("senha-atual-errada", "outra-senha-123");
        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRequest))
                        .session(session))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("INVALID_CREDENTIALS"));
    }

    @Test
    void logoutInvalidaASessao() throws Exception {
        var session = login(INITIAL_USERNAME, INITIAL_PASSWORD);

        mockMvc.perform(post("/api/auth/logout").session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    private MockHttpSession login(String username, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequest(username, password))))
                .andExpect(status().isOk())
                .andReturn();
        return (MockHttpSession) result.getRequest().getSession(false);
    }
}
