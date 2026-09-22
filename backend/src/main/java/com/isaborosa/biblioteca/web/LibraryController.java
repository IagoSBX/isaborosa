package com.isaborosa.biblioteca.web;

import com.isaborosa.biblioteca.domain.userbook.ReadingStatus;
import com.isaborosa.biblioteca.dto.AddToLibraryRequest;
import com.isaborosa.biblioteca.dto.UpdateLibraryRequest;
import com.isaborosa.biblioteca.dto.UserBookResponseDto;
import com.isaborosa.biblioteca.service.LibraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/library")
@Tag(name = "Biblioteca", description = "Estante pessoal: adicionar, listar, atualizar status/nota e remover livros")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping
    @Operation(summary = "Lista os livros da biblioteca pessoal", description = "Filtro opcional por status (QUERO_LER, LENDO, LIDO).")
    public List<UserBookResponseDto> list(@RequestParam(required = false) ReadingStatus status) {
        return libraryService.list(status);
    }

    @PostMapping
    @Operation(summary = "Adiciona um livro a biblioteca pessoal com um status inicial")
    public ResponseEntity<UserBookResponseDto> add(@Valid @RequestBody AddToLibraryRequest request) {
        UserBookResponseDto created = libraryService.add(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{userBookId}")
    @Operation(summary = "Atualiza status e/ou nota de um livro da biblioteca")
    public UserBookResponseDto update(@PathVariable Long userBookId, @Valid @RequestBody UpdateLibraryRequest request) {
        return libraryService.update(userBookId, request);
    }

    @DeleteMapping("/{userBookId}")
    @Operation(summary = "Remove um livro da biblioteca pessoal")
    public ResponseEntity<Void> remove(@PathVariable Long userBookId) {
        libraryService.remove(userBookId);
        return ResponseEntity.noContent().build();
    }
}
