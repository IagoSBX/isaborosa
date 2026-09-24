package com.isaborosa.biblioteca.web;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isaborosa.biblioteca.domain.book.Book;
import com.isaborosa.biblioteca.domain.book.BookRepository;
import com.isaborosa.biblioteca.domain.user.User;
import com.isaborosa.biblioteca.domain.user.UserRepository;
import com.isaborosa.biblioteca.domain.userbook.UserBookRepository;
import com.isaborosa.biblioteca.dto.AddToLibraryRequest;
import com.isaborosa.biblioteca.dto.UpdateLibraryRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@WithMockUser(username = "Isaborosa")
class LibraryControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private UserBookRepository userBookRepository;

    private Book book;

    @BeforeEach
    void setUp() {
        userBookRepository.deleteAll();
        bookRepository.deleteAll();
        userRepository.deleteAll();
        userRepository.flush();

        userRepository.save(new User("Isaborosa", "Isaborosa", "{bcrypt}unused-in-this-test", false));
        book = bookRepository.save(new Book("O Hobbit", "J.R.R. Tolkien", null, null, 1937, null, null, null, null, null));
    }

    @Test
    void deveAdicionarListarAtualizarERemoverDaBiblioteca() throws Exception {
        AddToLibraryRequest addRequest = new AddToLibraryRequest(book.getId(), com.isaborosa.biblioteca.domain.userbook.ReadingStatus.QUERO_LER, null, null);

        String response = mockMvc.perform(post("/api/library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("QUERO_LER"))
                .andReturn().getResponse().getContentAsString();

        long userBookId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/library"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        UpdateLibraryRequest updateRequest = new UpdateLibraryRequest(com.isaborosa.biblioteca.domain.userbook.ReadingStatus.LIDO, 5, null, null);
        mockMvc.perform(patch("/api/library/{id}", userBookId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("LIDO"))
                .andExpect(jsonPath("$.rating").value(5));

        mockMvc.perform(delete("/api/library/{id}", userBookId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/library"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void deveRecusarLivroDuplicadoNaBibliotecaCom409() throws Exception {
        AddToLibraryRequest addRequest = new AddToLibraryRequest(book.getId(), com.isaborosa.biblioteca.domain.userbook.ReadingStatus.QUERO_LER, null, null);

        mockMvc.perform(post("/api/library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("DUPLICATE_BOOK_IN_LIBRARY"));
    }

    @Test
    void deveRetornar404AoAtualizarItemInexistente() throws Exception {
        UpdateLibraryRequest updateRequest = new UpdateLibraryRequest(com.isaborosa.biblioteca.domain.userbook.ReadingStatus.LIDO, null, null, null);

        mockMvc.perform(patch("/api/library/{id}", 9999)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("USER_BOOK_NOT_FOUND"));
    }

    @Test
    void deveRecusarBookIdAusenteCom400() throws Exception {
        String invalidBody = "{\"status\":\"LENDO\"}";

        mockMvc.perform(post("/api/library")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }
}
