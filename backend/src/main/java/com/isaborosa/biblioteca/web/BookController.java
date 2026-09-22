package com.isaborosa.biblioteca.web;

import com.isaborosa.biblioteca.dto.BookResponseDto;
import com.isaborosa.biblioteca.dto.BookSearchResultDto;
import com.isaborosa.biblioteca.dto.CreateBookRequest;
import com.isaborosa.biblioteca.dto.PriceDto;
import com.isaborosa.biblioteca.service.BookService;
import com.isaborosa.biblioteca.service.PriceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/books")
@Tag(name = "Livros", description = "Busca externa (Open Library) e catalogo local de livros")
public class BookController {

    private final BookService bookService;
    private final PriceService priceService;

    public BookController(BookService bookService, PriceService priceService) {
        this.bookService = bookService;
        this.priceService = priceService;
    }

    @GetMapping("/search")
    @Operation(
            summary = "Busca livros combinando Open Library e Google Books",
            description = "Paginado por 'page' (0-indexado) para scroll infinito. Nunca falha com 500 por indisponibilidade de uma fonte; resultados sao combinados e sem duplicatas.")
    public List<BookSearchResultDto> search(
            @RequestParam String q, @RequestParam(defaultValue = "0") int page) {
        return bookService.search(q, page);
    }

    @GetMapping("/popular")
    @Operation(summary = "Livros em alta na Open Library", description = "Dado real de tendencia, nunca inventado.")
    public List<BookSearchResultDto> popular() {
        return bookService.popular();
    }

    @GetMapping("/browse")
    @Operation(
            summary = "Lista livros de um genero",
            description = "Busca por assunto (mais precisa que texto livre) para navegacao por categoria. Mesmas fontes e paginacao do /search.")
    public List<BookSearchResultDto> browse(
            @RequestParam String genre, @RequestParam(defaultValue = "0") int page) {
        return bookService.searchByGenre(genre, page);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Detalhes de um livro ja salvo localmente")
    public BookResponseDto getById(@PathVariable Long id) {
        return bookService.getById(id);
    }

    @PostMapping
    @Operation(summary = "Cria ou atualiza (upsert) um livro local a partir de um resultado de busca")
    public ResponseEntity<BookResponseDto> create(@Valid @RequestBody CreateBookRequest request) {
        BookResponseDto created = bookService.upsert(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}/prices")
    @Operation(
            summary = "Lista precos reais encontrados para o livro",
            description = "Consulta fontes externas em paralelo (hoje, Mercado Livre). Fonte que falha e omitida; nenhum preco e inventado.")
    public List<PriceDto> prices(@PathVariable Long id) {
        return priceService.getPrices(id);
    }
}
