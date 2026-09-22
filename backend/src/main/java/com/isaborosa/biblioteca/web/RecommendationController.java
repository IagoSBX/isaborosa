package com.isaborosa.biblioteca.web;

import com.isaborosa.biblioteca.dto.RecommendationDto;
import com.isaborosa.biblioteca.service.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/recommendations")
@Tag(name = "Recomendacoes", description = "Sugestoes de leitura baseadas nos livros bem avaliados na biblioteca")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(RecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    @GetMapping
    @Operation(
            summary = "Lista recomendacoes de livros",
            description = "Baseado nos autores dos livros avaliados com nota >= 4. Resultado cacheado por 1h.")
    public List<RecommendationDto> list() {
        return recommendationService.getRecommendations();
    }
}
