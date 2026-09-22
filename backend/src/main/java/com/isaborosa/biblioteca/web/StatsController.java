package com.isaborosa.biblioteca.web;

import com.isaborosa.biblioteca.dto.StatsDto;
import com.isaborosa.biblioteca.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/library/stats")
@Tag(name = "Estatisticas", description = "Agregacoes sobre a biblioteca pessoal (status, paginas, generos)")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    @Operation(summary = "Estatisticas da biblioteca pessoal")
    public StatsDto getStats() {
        return statsService.getStats();
    }
}
